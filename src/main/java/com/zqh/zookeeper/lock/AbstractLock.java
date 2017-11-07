package com.zqh.zookeeper.lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.ZooKeeper;

public abstract class AbstractLock implements Lock {

    protected ZooKeeper zooKeeper;

    protected boolean validate(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name must not be empty");
        }
        if (!zooKeeper.getState().isAlive()) {
            throw new RuntimeException("session expired");
        }
//        if (!zooKeeper.getState().isConnected()) {
//            System.out.println("session connecting");
//            return false;
//        }
        return true;
    }
}
