package com.zqh.zookeeper.lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * ZKLock Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre></pre>
 */
public class ZKLockTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: tryLock(@NotEmpty String name)
     */
    @Test
    public void testTryLockName() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: tryLock(@NotEmpty String name, long timeout, TimeUnit unit)
     */
    @Test
    public void testTryLockForNameTimeoutUnit() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: lock(String name)
     */
    @Test
    public void testLock() throws Exception {
        CyclicBarrier cb = new CyclicBarrier(5);
        CountDownLatch cd = new CountDownLatch(5);
        for (int i=0; i<5; i++) {
            Thread td = new Thread(new Runnable() {
                @Override
                public void run() {
                    ZKLock zkLock = null;
                    try {
                        cb.await();
                        zkLock = new ZKLock("", "", 30000);
                        long start = System.currentTimeMillis();
                        zkLock.lock("test");
                        System.out.println(Thread.currentThread().getName() + "=======> 获取到锁，花费时长为 " + (System.currentTimeMillis()-start) + "毫秒");
                        TimeUnit.SECONDS.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != zkLock) {
                            System.out.println(Thread.currentThread().getName() + "=======> 释放锁");
                            zkLock.unlock("test");
                        }
                        cd.countDown();
                    }


                }
            });
            td.start();
        }
       cd.await();
    }

    /**
     * Method: unlock(String name)
     */
    @Test
    public void testUnlock() throws Exception {
//TODO: Test goes here... 
    }

}
