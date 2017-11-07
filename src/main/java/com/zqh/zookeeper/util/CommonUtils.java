package com.zqh.zookeeper.util;

import sun.management.VMManagement;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.management.ManagementFactory.*;

public class CommonUtils {

    public static int getJvmPid() throws Exception {
        RuntimeMXBean runtime = getRuntimeMXBean();
        Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        VMManagement mgmt = (VMManagement) jvm.get(runtime);
        Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
        pidMethod.setAccessible(true);
        int pid = (Integer) pidMethod.invoke(mgmt);
        return pid;
    }

    public static String getInstanceId() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append(InetUtils.getMAC())
                .append("]-")
                .append(getJvmPid())
                .append("-")
                .append(Thread.currentThread().getId())
                .append("-[")
                .append(Thread.currentThread().getName())
                .append("]");
        return sb.toString();
    }

    public static String pathHandler(String path) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        path = path.startsWith("/") ? path : "/" + path;
        return path;
    }

}
