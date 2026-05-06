package com.ourexists.omes.portal.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.NamedNode;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从与 {@link org.springframework.data.redis.core.RedisTemplate} 相同的 {@link RedisConnectionFactory}
 * 推导地址与认证，创建 {@link RedissonClient}，避免 Redisson 与 Lettuce 各读一套、连接池双份。
 */
@Configuration
public class RedissonClientConfiguration {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisConnectionFactory redisConnectionFactory) {
        if (!(redisConnectionFactory instanceof LettuceConnectionFactory lcf)) {
            throw new IllegalStateException(
                    "Redisson 仅支持 Lettuce 的 RedisConnectionFactory，当前: "
                            + redisConnectionFactory.getClass().getName());
        }
        String scheme = lcf.isUseSsl() ? "rediss://" : "redis://";
        Config config = new Config();
        RedisClusterConfiguration cluster = lcf.getClusterConfiguration();
        if (cluster != null && !cluster.getClusterNodes().isEmpty()) {
            applyCluster(config, cluster, scheme);
        } else {
            RedisSentinelConfiguration sentinel = lcf.getSentinelConfiguration();
            if (sentinel != null && !sentinel.getSentinels().isEmpty()) {
                applySentinel(config, sentinel, scheme);
            } else {
                RedisStandaloneConfiguration standalone = lcf.getStandaloneConfiguration();
                if (standalone == null) {
                    throw new IllegalStateException("LettuceConnectionFactory 未提供 standalone/cluster/sentinel 配置");
                }
                applyStandalone(config, standalone, scheme);
            }
        }
        return Redisson.create(config);
    }

    private static void applyStandalone(Config config, RedisStandaloneConfiguration c, String scheme) {
        SingleServerConfig s = config.useSingleServer();
        s.setAddress(scheme + c.getHostName() + ":" + c.getPort());
        s.setDatabase(c.getDatabase());
        applyUsernamePassword(s, c.getUsername(), c.getPassword());
    }

    private static void applyCluster(Config config, RedisClusterConfiguration c, String scheme) {
        List<String> nodes = new ArrayList<>();
        for (RedisNode node : c.getClusterNodes()) {
            nodes.add(scheme + node.getHost() + ":" + node.getPort());
        }
        var clusterServers = config.useClusterServers().addNodeAddress(nodes.toArray(String[]::new));
        applyUsernamePassword(clusterServers, c.getUsername(), c.getPassword());
    }

    private static void applySentinel(Config config, RedisSentinelConfiguration c, String scheme) {
        SentinelServersConfig s = config.useSentinelServers();
        NamedNode master = c.getMaster();
        if (master == null || !StringUtils.hasText(master.getName())) {
            throw new IllegalStateException("Redis Sentinel 未配置 master");
        }
        s.setMasterName(master.getName());
        for (RedisNode node : c.getSentinels()) {
            s.addSentinelAddress(scheme + node.getHost() + ":" + node.getPort());
        }
        s.setDatabase(c.getDatabase());
        applyUsernamePassword(s, c.getUsername(), c.getPassword());
    }

    private static void applyUsernamePassword(
            SingleServerConfig target,
            String username,
            RedisPassword password) {
        if (StringUtils.hasText(username)) {
            target.setUsername(username);
        }
        String pwd = passwordToString(password);
        if (pwd != null) {
            target.setPassword(pwd);
        }
    }

    private static void applyUsernamePassword(
            org.redisson.config.ClusterServersConfig target,
            String username,
            RedisPassword password) {
        if (StringUtils.hasText(username)) {
            target.setUsername(username);
        }
        String pwd = passwordToString(password);
        if (pwd != null) {
            target.setPassword(pwd);
        }
    }

    private static void applyUsernamePassword(
            SentinelServersConfig target,
            String username,
            RedisPassword password) {
        if (StringUtils.hasText(username)) {
            target.setUsername(username);
        }
        String pwd = passwordToString(password);
        if (pwd != null) {
            target.setPassword(pwd);
        }
    }

    private static String passwordToString(RedisPassword password) {
        if (password == null || !password.isPresent()) {
            return null;
        }
        return new String(password.get());
    }
}
