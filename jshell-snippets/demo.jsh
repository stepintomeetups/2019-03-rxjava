import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
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
    }

    public Observable<Long> delays() {
        final var delays = List.of(2L, 5L, 3L, 2L, 3L);
        return Observable.fromIterable(delays);
    }

    public Observable<Double> temperatures() {
        return Observable.fromArray(20.23, 20.21, 20.14, 20.09, 20.14);
    }

    public Observable<Double> temperaturesFromSecondSensor() {
        return Observable.fromArray(19.23, 19.22, 19.22, 19.22, 19.21);
    }

    public Observable<Double> fastEmitter() {
        return temperatureGenerator(temperatures(), 1, TimeUnit.NANOSECONDS);
    }

    public Observable<Double> sensor1() {
        Flowable.just(1).subscribe();
        return temperatureGenerator(temperatures(), 1, TimeUnit.SECONDS);
    }

    public Observable<Double> sensor2() {
        return temperatureGenerator(temperaturesFromSecondSensor(), 1, TimeUnit.SECONDS);
    }

    public Observable<Double> temperatureGenerator(final Observable<Double> temperatures, final long interval, final TimeUnit timeUnit) {
        return Observable.zip(temperatures, delays(), (temperature, delay) -> new DelayedTemperature(temperature, delay, TimeUnit.SECONDS)).
                concatMap(delayedTemperature ->
                        Observable.interval(interval, timeUnit).
                                map(tick -> delayedTemperature.getTemperature()).take(delayedTemperature.getDelay(), delayedTemperature.getTimeUnit())
                );
    }

    public Observable<Double> someDouble() {
        return Observable.fromCallable(() -> ThreadLocalRandom.current().nextDouble());
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
            System.out.println("Finished with error on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now() + " with exception" + throwable.getStackTrace());
        }

        @Override
        public void onComplete() {
            System.out.println("Work done on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }
    }

    class TracingSubscriber implements Subscriber<Double> {
        @Override
        public void onSubscribe(final Subscription subscription) {
            subscription.request(Integer.MAX_VALUE);
        }

        @Override
        public void onNext(final Double item) {
            System.out.println("Received " + item + " on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }

        @Override
        public void onError(final Throwable throwable) {
            System.out.println("Finished with error on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now() + " with exception" + throwable.getStackTrace());

        }

        @Override
        public void onComplete() {
            System.out.println("Work done on thread " + Thread.currentThread().getName() +
                    " @ " + LocalTime.now());
        }
    }

