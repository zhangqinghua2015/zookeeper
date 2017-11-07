package com.zqh.zookeeper.lock;

import com.zqh.zookeeper.util.CommonUtils;
import org.apache.zookeeper.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SequentialNodeZKLock extends AbstractLock {

    private static final String lockName = "/lock-";

    private final ThreadLocal<ConcurrentHashMap<String, String>> threadOwnPath = new ThreadLocal<>();

    public SequentialNodeZKLock(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public boolean tryLock(String name) {
        return tryLock(name, 0, null);
    }

    @Override
    public boolean tryLock(String name, long timeout, TimeUnit unit) {
        if (!validate(name)) {
            return false;
        }
        name = CommonUtils.pathHandler(name);
        boolean needDelete = false;
        String ownPath = null;
        try {
            if (null == zooKeeper.exists(name, false)) {
                try {
                    zooKeeper.create(name, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } catch (KeeperException.NodeExistsException e) {
                    // 不处理
                }
            }
            ownPath = zooKeeper.create(name + lockName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> childrenList = zooKeeper.getChildren(name, false);
            Collections.sort(childrenList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            int index = childrenList.indexOf(ownPath.substring(name.length() + 1));
            if (0 == index) {
                System.out.println("直接获取锁");
                putOwnPath(name, ownPath);
                return true;
            }
            if (timeout == 0) {
                System.out.println("未获取到锁");
                needDelete = true;
                return false;
            }
            String watchingPath = childrenList.get(index - 1);
            CountDownLatch cd = new CountDownLatch(1);
            try {
                final String delOwnPath = ownPath;
                zooKeeper.getData(name + CommonUtils.pathHandler(watchingPath), new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (cd.getCount() != 1) {
                            System.out.println("该锁已获取超时，删除该节点");
                            try {
                                zooKeeper.delete(delOwnPath, -1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("唤醒下一个");
                            cd.countDown();
                        }
                    }
                }, null);
                System.out.println("等待锁");
                if (timeout > 0) {
                    if (!cd.await(timeout, unit)) {
                        cd.countDown();
                        return false;
                    }
                } else {
                    cd.await();
                }
            } catch(KeeperException.NoNodeException e) {
                // 直接获取到了
                System.out.println("恰好锁释放了，不用等待，直接获取");
            }
            putOwnPath(name, ownPath);
        } catch (Exception e) {
            return false;
        } finally {
            if (needDelete) {
                doUnlock(ownPath);
            }
        }
        return true;
    }

    @Override
    public void lock(String name) {
        while (!tryLock(name, -1, null)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unlock(String name) {
        if (!validate(name)) {
            return;
        }
        name = CommonUtils.pathHandler(name);
        ConcurrentHashMap<String, String> ownPathMap = threadOwnPath.get();
        if (null == ownPathMap) {
            return;
        }
        String ownPath = ownPathMap.remove(name);
        if (!validate(ownPath)) {
            return;
        }
        doUnlock(ownPath);
    }

    private void doUnlock(String ownPath) {
        try {
            zooKeeper.delete(ownPath, -1);
        } catch(KeeperException.NoNodeException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putOwnPath(String name, String ownPath) {
        ConcurrentHashMap<String, String> ownPathMap = threadOwnPath.get();
        if (null == ownPathMap) {
            ownPathMap = new ConcurrentHashMap<>();
        }
        ownPathMap.put(name, ownPath);
        threadOwnPath.set(ownPathMap);
    }


}
