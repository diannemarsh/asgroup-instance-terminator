package com.sample.autoscaling;

import com.sample.autoscaling.config.Config;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main class to run the application
 */
public final class App {

    private static final Object LOCK = new Object();

    private App() {
        //Prevents Instantiation
    }

    public static void main(String... args) throws InterruptedException {

        //Initialize Application Context
        new AnnotationConfigApplicationContext(Config.class);

        synchronized (LOCK) {
            LOCK.wait();
        }
    }
}
