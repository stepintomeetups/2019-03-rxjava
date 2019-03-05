# Step into Reactive // SIM 2019/03

<p align="center">
<img src="assets/logo.svg" style="width:800px; height:220px; display:inline;">
</p>

---

## Topics
- **Observer design pattern, Iterator design pattern, streams, Java `Future`s and `CompletableFuture`s** 
- **Reactive programming and RxJava**
- **Let's code!**
- **Reactive programming vs functional reactive programming vs reactive systems**
- **Questions**

---

## Prerequisites
JShell snippets were developed and tested with the following toolchain, 
however in theory everything should work fine with any JDK 10+ 
(or JDK 9+ if you replace `var` keywords with the proper types)

### JDK (Oracle JDK)
```cmd
java version "11.0.2" 2018-10-16 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.2+7-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.2+7-LTS, mixed mode)
```

### JShell (Oracle JDK)
```cmd
|  Welcome to JShell -- Version 11.0.2
```
Make sure that `<JDK_11_installation>\bin` is on your classpath.

In case of you have multiple JDK installation with JShell support on your classpath
then you may have to rearrange your classpath so `<JDK_11_installation>\bin` is checked for JShell before any other JDKs.

### IDEA
```cmd
IntelliJ IDEA 2108.3.5 (Ultimate Edition) 
```

#### Why IDEA project files are put under version control
This sample project does not utilize any dependency management tool but by providing a preconfigured
IntelliJ IDEA project library sources, JavaDoc and target JDK is prepared for you to ease experimenting.

## How to run
It's assumed that at this point you have checked out the repo and all your tools are set up and ready for use.

### Start JShell

#### Outside of IDEA
- Fire up a console of your choice 
- Navigate to the project folder 
- Start JShell as
```cmd
jshell --class-path ./libs/*
```

#### In IDEA
- Open the project in IDEA
- Right click on the project and select `Open in Terminal`
- Within the console execute
```cmd
jshell --class-path ./libs/*
```

You may have to associate `jsh` file types (as `JShell Snippet`) if code completion, show JavaDoc or jump to sources does not work. 

### Working with JShell
- As a first step execute
```cmd
 /set start jshell-snippets/init.jsh
```
This will register a startup script snippet which later can be used to reset the shell and reload the content of `demo.jsh`. 

- followed by
```cmd
/reset
```
Now everything should be ready, you can execute `/reset` anytime to discard your changes and reload `demo.jsh`.

- and when you're done you can just simply exit
```cmd
/exit
``` 

## Useful sources
- [JShell intro](https://docs.oracle.com/javase/10/jshell/introduction-jshell.htm)
- [RxJava GitHub repo](https://github.com/ReactiveX/RxJava)
- [ReactiveX homepage](http://reactivex.io/)
- [Learning RxJava (RxJava 2)](https://www.amazon.com/Learning-RxJava-Thomas-Nield/dp/1787120422)
- [Reactive Programming with RxJava (RxJava 1)](https://www.amazon.com/Reactive-Programming-RxJava-Asynchronous-Applications/dp/1491931655)
- [Dávid Karnok's blog](http://akarnokd.blogspot.com/)
