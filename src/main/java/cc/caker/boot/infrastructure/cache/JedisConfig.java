package cc.caker.boot.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

/**
 * JedisConfig
 *
 * @author cakeralter
 * @date 2021/8/28
 * @since 1.0
 */
@Slf4j
public final class JedisConfig {

    private JedisCluster jedisCluster;

    @PostConstruct
    public void init() {
        Set<HostAndPort> nodes = new HashSet<>(2);
        nodes.add(new HostAndPort("81.69.255.167", 6379));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(4);

        jedisCluster = new JedisCluster(
                nodes,
                30,
                30,
                5,
                "redispassword",
                poolConfig);
    }

    @PreDestroy
    public void destroy() {
        jedisCluster.close();
        log.info("");
    }
}
