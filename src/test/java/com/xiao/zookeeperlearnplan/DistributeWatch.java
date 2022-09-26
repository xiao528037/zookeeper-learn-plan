package com.xiao.zookeeperlearnplan;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "de")
@SpringBootTest(properties = "logging.level.root=info")
public class DistributeWatch {
    private static String CLUSTER_ZOOKEEPER = "zk_node01:2181,zk_node02:2181,zk_node03:2181";;
    private static ZooKeeper client;
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        //创建链接
        createConnection();
        //监听
        watchPath("/servers");
        TimeUnit.SECONDS.sleep(100000);
    }

    private static void watchPath(String node) throws InterruptedException, KeeperException {
        List<String> watch = client.getChildren(node, true);
        ArrayList<String> znodes=new ArrayList<>();
        for (String path : watch) {
            byte[] nodeData = client.getData(node + "/" + path, false, null);
            if (nodeData!=null){
                znodes.add(new String(nodeData));
            }

        }
        log.info(">>>>>> {}",znodes);
    }

    public static void createConnection() throws IOException, InterruptedException, KeeperException {
        client=new ZooKeeper(CLUSTER_ZOOKEEPER,2000,event -> {
            try {
                watchPath("/servers");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
