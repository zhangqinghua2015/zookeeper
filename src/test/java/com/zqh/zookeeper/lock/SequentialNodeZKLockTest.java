package com.zqh.zookeeper.lock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * SequentialNodeZKLock Tester.
 *
 * @author <Authors name>
 * @version 1.0
 */
public class SequentialNodeZKLockTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: tryLock(String name)
     */
    @Test
    public void testTryLockName() throws Exception {
        ZooKeeper zk = new ZooKeeper("", 30000, null);
        SequentialNodeZKLock lock = new SequentialNodeZKLock(zk);

        CyclicBarrier cb = new CyclicBarrier(2);
        CountDownLatch cd = new CountDownLatch(2);
        for (int i=0; i<2; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.await();
                        long start = System.currentTimeMillis();
                        if (lock.tryLock("test-tryLock")) {
                            try {
                                System.out.println(Thread.currentThread().getName() + "=======> 获取到锁，花费时长为 " + (System.currentTimeMillis() - start) + "毫秒");
                                TimeUnit.SECONDS.sleep(20);
                            }  finally {
                                System.out.println(Thread.currentThread().getName() + "=======> 释放锁");
                                lock.unlock("test-tryLock");
                            }
                        } else {
                            System.out.println(Thread.currentThread().getName() + "=======> 未获取到锁");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cd.countDown();
                }
            }, "test-tryLock thread-" + i).start();
        }
        cd.await();
    }

    /**
     * Method: tryLock(String name, long timeout, TimeUnit unit)
     */
    @Test
    public void testTryLockForNameTimeoutUnit() throws Exception {
        ZooKeeper zk = new ZooKeeper("", 30000, null);
        SequentialNodeZKLock lock = new SequentialNodeZKLock(zk);

        for (int i=0; i<10; i++) {
            final long m = i == 9 ? 30 : i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        if (lock.tryLock("test-tryLockWithTimeout", 5 + m, TimeUnit.SECONDS)) {
                            try {
                                System.out.println(Thread.currentThread().getName() + "=======> 获取到锁，花费时长为 " + (System.currentTimeMillis() - start) + "毫秒");
                                TimeUnit.SECONDS.sleep(30);
                            }  finally {
                                System.out.println(Thread.currentThread().getName() + "=======> 释放锁");
                                lock.unlock("test-tryLockWithTimeout");
                            }
                        } else {
                            System.out.println(Thread.currentThread().getName() + "=======> 未获取到锁，等待时长为 " + (System.currentTimeMillis() - start) + "毫秒");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "test-tryLock thread-" + i).start();
        }
        TimeUnit.SECONDS.sleep(100);
    }

    /**
     * Method: lock(String name)
     */
    @Test
    public void testLock() throws Exception {
        ZooKeeper zk = new ZooKeeper("", 30000, null);
        SequentialNodeZKLock lock = new SequentialNodeZKLock(zk);

        CyclicBarrier cb = new CyclicBarrier(5);
        CountDownLatch cd = new CountDownLatch(5);
        for (int i=0; i<2; i++) {
           new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.await();
                        long start = System.currentTimeMillis();
                        lock.lock("test");
                        System.out.println(Thread.currentThread().getName() + "=======> 获取到锁，花费时长为 " + (System.currentTimeMillis()-start) + "毫秒");
                        TimeUnit.SECONDS.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println(Thread.currentThread().getName() + "=======> 释放锁");
                        lock.unlock("test");
                        cd.countDown();
                    }


                }
            }, "test-lock thread-" + i).start();
        }
        for (int i=0; i<3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.await();
                        long start = System.currentTimeMillis();
                        lock.lock("test2");
                        System.out.println(Thread.currentThread().getName() + "=======> 获取到锁，花费时长为 " + (System.currentTimeMillis()-start) + "毫秒");
                        TimeUnit.SECONDS.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println(Thread.currentThread().getName() + "=======> 释放锁");
                        lock.unlock("test2");
                        cd.countDown();
                    }


                }
            }, "test2-lock thread-" + i).start();
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
