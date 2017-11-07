package com.zqh.zookeeper.lock;

import com.zqh.zookeeper.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ZKLock extends AbstractLock {

    private String nameSpace;

    public ZKLock(String nameSpace, String connectString, int sessionTimeout) throws IOException {
        this.zooKeeper = new ZooKeeper(connectString, sessionTimeout, null);
        nameSpace = StringUtils.isBlank(nameSpace) ? "/ZKLock" : nameSpace.endsWith("/") ? nameSpace : nameSpace.substring(0, nameSpace.length() - 1);
        nameSpace = nameSpace.startsWith("/") ? nameSpace : ("/" + nameSpace);
        this.nameSpace = nameSpace;
    }

    @Override
    public boolean tryLock(String name) {
        if (!validate(name)) {
           return false;
        }
        try {
           doLock(name);
        } catch (Exception e) {
            if (e instanceof KeeperException && ((KeeperException) e).code() == KeeperException.Code.NODEEXISTS) {
                return false;
            }
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean tryLock(String name, long timeout, TimeUnit unit) {
        if (timeout <= 0) {
            return tryLock(name);
        }
        long startTime = System.currentTimeMillis();
        if (TimeUnit.SECONDS.equals(unit)) {
            timeout = unit.toMillis(timeout);
        }
        while (!tryLock(name)) {
            if (System.currentTimeMillis() - startTime >= timeout) {
                return false;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void lock(String name) {
        while (!tryLock(name)) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unlock(String name) {
        try {
            String path = nameSpace + CommonUtils.pathHandler(name);
            byte[] date = zooKeeper.getData(path, null, null);
            if (CommonUtils.getInstanceId().equals(new String(date))) {
                zooKeeper.delete(path, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doLock(String name) throws Exception {
        name = CommonUtils.pathHandler(name);
        if (null == zooKeeper.exists(nameSpace, false)) {
            zooKeeper.create(nameSpace, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        zooKeeper.create(nameSpace + name, CommonUtils.getInstanceId().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }






    public static void main(String[] args) {
        System.out.println("/test/aaa".substring("/test".length() + 1));

    }
}
