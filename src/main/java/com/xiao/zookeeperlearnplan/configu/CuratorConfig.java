package com.xiao.zookeeperlearnplan.configu;

import com.xiao.zookeeperlearnplan.properties.WrapperZk;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuratorConfig {

    private final WrapperZk wrapperZk;

    public CuratorConfig(WrapperZk wrapperZk) {
        this.wrapperZk = wrapperZk;
    }

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                wrapperZk.getConnectString(),
                wrapperZk.getSessionTimeoutMs(),
                wrapperZk.getConnectionTimeoutMs(),
                //多少秒后重试，重试多少次
                new RetryNTimes(
                        wrapperZk.getRetryCount(),
                        wrapperZk.getElapsedTimeMs()
                ));
    }
}
