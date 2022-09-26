package com.xiao.zookeeperlearnplan;

import com.sun.org.apache.bcel.internal.generic.FADD;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;


class ZookeeperLearnPlanApplicationTests {
    private static String CLUSTER_ZOOKEEPER = "zk_node01:2181,zk_node02:2181,zk_node03:2181";
    private ZooKeeper client;

    @BeforeEach
    public void before() throws IOException {

        client = new ZooKeeper(CLUSTER_ZOOKEEPER,2000, watchedEvent -> {
            try {
                List<String> children = client.getChildren("/", true);
                System.out.println("--------------------------------------------");
                for (String child : children) {
                    System.out.println(child);
                }
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("创建成功");
    }

    @Test
    public void createPath() throws InterruptedException, KeeperException {
        client.create("/xiao","shuaige".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    void t1() throws InterruptedException, KeeperException {
        List<String> children = client.getChildren("/", true);
        for (String child : children) {
            System.out.println(child);
        }
        System.out.println("--------------------------------------------");
        TimeUnit.SECONDS.sleep(10000);
    }

    @Test
    public void exist() throws InterruptedException, KeeperException {
        Stat exists = client.exists("/xiao", false);
        if (exists==null){
            System.out.println("没有");
        }
        System.out.println(exists.getVersion());

    }

}
