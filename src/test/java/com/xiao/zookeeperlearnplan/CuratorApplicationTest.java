package com.xiao.zookeeperlearnplan;

import com.xiao.zookeeperlearnplan.configu.CuratorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper分布式锁
 */
@SpringBootTest(classes = ZookeeperLearnPlanApplication.class)
@Slf4j(topic = ">>> ")
public class CuratorApplicationTest {
    @Resource
    private CuratorFramework curatorFramework;

    private Integer count = 0;

    /**
     * 创建节点
     *
     * @throws Exception
     */
    @Test
    void createNode() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath("/locks");
        String path = null;
        if (stat == null) {
            path = curatorFramework.create().forPath("/locks");
        } else {
            log.info("{},永久节点已经存在", "/locks");
        }
        String s = curatorFramework.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath("/locks/seq-", "seq-".getBytes());
        log.info("{},临时节点创建成功", s);
        TimeUnit.SECONDS.sleep(1000);
    }

    /**
     * 分布式锁
     */
    @Test
    void lock() throws InterruptedException {
        //获取到分布式锁
        InterProcessMutex lockOne = new InterProcessMutex(curatorFramework, "/locks");
        InterProcessMutex lockTwo = new InterProcessMutex(curatorFramework, "/locks");
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    lockOne.acquire();
                    log.info("{}", "获取到锁");
                    count++;
                    lockOne.release();
                    log.info("{}", "释放锁");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    lockTwo.acquire();
                    log.info("{}", "获取到锁");
                    count++;
                    lockTwo.release();
                    log.info("{}", "释放锁");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        TimeUnit.SECONDS.sleep(5);
        System.out.println(count);
    }

    /**
     * 读写锁
     */

    @Test
    void lockt() {
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(curatorFramework, "/locks");
        new Thread(() -> {
            readWriteLock.readLock();
        }).start();
    }
}
