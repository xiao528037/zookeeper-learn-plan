package com.xiao.zookeeperlearnplan.zookeeperLock;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class ZookeeperLock {
    private static String CLUSTER_ZOOKEEPER = "zk_node01:2181,zk_node02:2181,zk_node03:2181";
    private static Integer CONNECTION_TIMEOUT = 2000;
    private static ZooKeeper client;

    private static Thread connThread = null;
    private static CountDownLatch waitLock = new CountDownLatch(1);
    private static String watchPath = null;

    public ZookeeperLock() throws Exception {
        try {
            client = new ZooKeeper(CLUSTER_ZOOKEEPER, CONNECTION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    LockSupport.unpark(connThread);
                }
                if (event.getType() == Watcher.Event.EventType.NodeDeleted && event.getType().equals(watchPath)) {
                    waitLock.countDown();
                }
            });
            connThread = Thread.currentThread();
            LockSupport.park();
            log.info(">>>> {}", "创建链接成功");
            rootExist();
        } catch (IOException | RuntimeException e) {

            log.error(">>>> {}", "链接失败");
            throw new Exception(e);
        }
    }

    /**
     * 判断根节点是否存在
     */
    private void rootExist() {
        try {
            Stat exists = client.exists("/locks", false);
            if (exists == null) {
                client.create("/locks", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(">>>> {}", "创建/locks根节点失败");
        }
    }

    /**
     * 获取锁
     */
    public void lock() {
        //创建一个带序号的零时节点
        try {
            String temp_lock = client.create("/locks/seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            //获取根节点下的所有临时锁node
            List<String> tem_locks = client.getChildren("/locks", true);
            if (tem_locks.size() == 1) {
                log.info(">>>> {}", "成功获取到锁");

            } else {
                //排序
                Collections.sort(tem_locks);
                String substring = temp_lock.substring("/locks/".length());
                int i = tem_locks.indexOf(substring);
                if (i == -1) {
                    log.error(">>>> {}", "获取锁失败");
                } else if (i == 0) {
                    log.info(">>>> {}", "成功获取到锁");
                } else {
                    watchPath = "/locks/" + tem_locks.get(i - 1);
                    client.getData(watchPath, true, null);
                    //等待其他线程执行完成
//                    waitThread = Thread.currentThread();
//                    LockSupport.park();
                    waitLock.await();
                    log.info(">>>> {}", "成功获取到锁");
                }

            }

        } catch (KeeperException | InterruptedException e) {
            log.error(">>>> {}", "创建锁失败");
        }
    }

    /**
     * 释放锁
     */
    public void unLock() {
        waitLock.countDown();
    }
}
