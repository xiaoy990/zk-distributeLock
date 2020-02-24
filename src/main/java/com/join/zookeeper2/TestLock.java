package com.join.zookeeper2;

import com.join.zookeeper2.util.DistributeLock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestLock {

    @Test
    public void testLock(){
        //模拟十个人去抢锁
        Thread[] threads = new Thread[10];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(()->{
                DistributeLock distributeLock = new DistributeLock();
                distributeLock.lock();
                System.out.println(Thread.currentThread().getName() +" working...");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                distributeLock.lock();
                System.out.println(Thread.currentThread().getName() +" working...");
                distributeLock.unlock();
                distributeLock.unlock();
            });
        }
        for (Thread t : threads) {
            t.start();
        }
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
