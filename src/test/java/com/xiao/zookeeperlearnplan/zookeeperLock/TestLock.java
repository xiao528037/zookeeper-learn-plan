package com.xiao.zookeeperlearnplan.zookeeperLock;



public class TestLock {
    static int i = 0;
    public static void main(String[] args) throws Exception {
        ZookeeperLock zkLock = new ZookeeperLock();
        ZookeeperLock zkLock1 = new ZookeeperLock();
        Thread T1 = new Thread(() -> {
            for (int j = 0; j < 100; j++) {
                zkLock.lock();
                try {
                    i++;
                }finally {
                    zkLock.unLock();
                }
            }
        });
        Thread T2 = new Thread(() -> {
            for (int j = 0; j < 100; j++) {
                zkLock1.lock();
                try {
                    i++;
                }finally {
                    zkLock1.unLock();
                }
            }
        });

        T1.start();
        T2.start();


        T1.join();
        T2.join();
        System.out.println(i);
    }
}
