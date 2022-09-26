package com.xiao.zookeeperlearnplan;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class DistributeServer {
    private static String CLUSTER_ZOOKEEPER = "zk_node01:2181,zk_node02:2181,zk_node03:2181";
    private static ZooKeeper client;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        //开启链接
        createConnection();
        //创建节点
        createZnode(args[0],System.getenv("ACSvcPort"));

    }

    private static void createZnode(String serverName,String port) throws InterruptedException, KeeperException {
        String s = client.create("/servers/" + serverName, port.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public static void createConnection() throws IOException {
        client = new ZooKeeper(CLUSTER_ZOOKEEPER, 2000, null);
        log.info("链接创建成功");
    }
}
