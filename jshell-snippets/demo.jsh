import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

    class DelayedTemperature {
        private final Double temperature;
        private final Long delay;
        private final TimeUnit timeUnit;

        public DelayedTemperature(Double temperature, Long delay, TimeUnit timeUnit) {
            this.temperature = temperature;
            this.delay = delay;
            this.timeUnit = timeUnit;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Long getDelay() {
            return delay;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        @Override
        public String toString() {
            return "DelayedTemperature{" +
                    "temperature=" + temperature +
                    ", delay=" + delay +
                    ", timeUnit=" + timeUnit +
                    '}';
        }
    }

    //sample delays used to change emitted temperatures after some time
    public Observable<Long> delays() {
        final var delays = List.of(2L, 5L, 3L, 2L, 3L);
        return Observable.fromIterable(delays);
    }

    //sample temperatures for sensor 1
    public Observable<Double> temperaturesFromFirstSensor() {
        return Observable.fromArray(20.23, 20.21, 20.14, 20.09, 20.14);
    }

    //sample temperatures for sensor 2
    public Observable<Double> temperaturesFromSecondSensor() {
        return Observable.fromArray(19.23, 19.22, 19.22, 19.22, 19.21);
    }

    //sample temperature sensor 1 emitting values over 15 seconds (20.23 for 2 seconds, 20.21 for 5 seconds,
    //20.14 for 3 seconds, 20.09 for 2 seconds and finally 20.14 for 3 seconds)
    public Observable<Double> sensor1() {
        return temperatureGenerator(temperaturesFromFirstSensor(), 1, TimeUnit.SECONDS);
    }

    //sample temperature sensor 2 emitting values over 15 seconds (19.23 for 2 seconds, 19.22 for 10 (5 + 3 + 2) seconds
    //and finally 19.21 for 3 seconds
    public Observable<Double> sensor2() {
        return temperatureGenerator(temperaturesFromSecondSensor(), 1, TimeUnit.SECONDS);
    }

    //zip temperatures with delays
    public Observable<DelayedTemperature> zip(final Observable<Double> temperatures) {
        return Observable.zip(temperatures, delays(), (temperature, delay) -> new DelayedTemperature(temperature, delay, TimeUnit.SECONDS));
    }

    //emit each temperature according to a predefined delay and continuously samples the stream at a given interval
    public Observable<Double> temperatureGenerator(final Observable<Double> temperatures, final long interval, final TimeUnit timeUnit) {
        return zip(temperatures).
                concatMap(delayedTemperature ->
                        Observable.interval(interval, timeUnit).
                                map(tick -> delayedTemperature.getTemperature()).take(delayedTemperature.getDelay(), delayedTemperature.getTimeUnit())
                );
    }

    //specialized type to represent an operation which may complete successfully or with an error
    public Single<Double> someDouble() {
        return Single.fromCallable(() -> ThreadLocalRandom.current().nextDouble());
    }

    //specialized type to represent an operation which may complete successfully, may not complete at all or complete with an error
    public Maybe<Integer> potentiallyFaultyOperation() {
        return Maybe.fromCallable(() -> 1 / 0);
    }

    //different elements for each observer
    public Observable<Double> doubles() {
        return Observable.defer(() -> {
            return Observable.just(ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble());
        });
    }

    //schedulers
    public Observable<Double> switchingBetweenSchedulers() {
        return Observable.just(1.1).
                doOnNext(item -> System.out.println("Producing on " + Thread.currentThread().getName())).
                subscribeOn(Schedulers.newThread()).
                observeOn(Schedulers.computation()).
                map(item -> item * 2).
                doOnNext(item -> System.out.println("Processing on " + Thread.currentThread().getName())).
                observeOn(Schedulers.io());
    }

    //cold vs hot Observables, multicasting
    public void multicast() {
        var shared = sensor1().share();

        shared.observeOn(Schedulers.io()).
                subscribe(item -> System.out.println("Writing to audit log: " + item));
        shared.map(elem -> round(elem)).
                distinctUntilChanged().
                subscribe(item -> System.out.println("Temperature changed: " + item));

        shared.timeout(500, TimeUnit.MILLISECONDS).
                retry(3).subscribe(elem -> {
        }, error -> System.out.println("Sensor is down!"));
    }

    //Backpressure enabled RxJava 2 types
    public Flowable<Double> flowable() {
        return Flowable.just(1.1, 2.2, 3.3);
    }


    public Double compute(final Double temperature) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5, 5000));
        } catch (final InterruptedException exc) {
            exc.printStackTrace();
        }

        return temperature;
    }

    public Double round(final Double item) {
        return BigDecimal.valueOf(item).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    //Test observer and subscriber

    class TracingObserver implements Observer<Double> {
        //by having a reference to disposable we could cancel the subscription any time within `onNext`
        private Disposable disposable;

        @Override
        public void onSubscribe(final Disposable disposable) {
            this.disposable = disposable;
        }

        @Override
        public void onNext(final Double item) {
            System.out.println("Received " + item + " on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }

        @Override
        public void onError(final Throwable throwable) {
            System.err.println("Finished with error on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now() + " with the following exception.");
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Work done on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }
    }

    class TracingSubscriber implements Subscriber<Double> {
        private Subscription subscription;

        @Override
        public void onSubscribe(final Subscription subscription) {
            this.subscription = subscription;
            System.out.println("Subscription done on thread " + Thread.currentThread().getName() +
                    ", initially requesting 2 items...");
            subscription.request(2L);
        }

        @Override
        public void onNext(final Double item) {
            System.out.println("Received " + item + " on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now() + ", requesting one more item...");
            subscription.request(1L);
        }

        @Override
        public void onError(final Throwable throwable) {
            System.err.println("Finished with error on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now() + " with the following exception.");
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Work done on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }
    }

