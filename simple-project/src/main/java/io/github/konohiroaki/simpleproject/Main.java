package io.github.konohiroaki.simpleproject;

import io.github.konohiroaki.annotationprocessor.timer.Timer;

public class Main {

    @Timer({3, 6})
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello0");
        Thread.sleep(300);
        System.out.println("Hello2");
        System.out.println("Hello3");
        System.out.println("Hello4");
        Thread.sleep(400);
        System.out.println("Hello6");
        System.out.println("Hello7");
        Thread.sleep(200);
        System.out.println("Hello9");
    }

    /* Should be transformed like this.
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello0");
        Thread.sleep(300L);
        System.out.println("Hello2");
        System.out.println("Timer Starting...");
        StopWatch timer = StopWatch.createStarted();
        System.out.println("Hello3");
        System.out.println("Hello4");
        Thread.sleep(400L);
        System.out.println("Hello6");
        timer.stop();
        System.out.println(timer.getTime(TimeUnit.MILLISECONDS));
        System.out.println("Hello7");
        Thread.sleep(200L);
        System.out.println("Hello9");
    }
    */
}
