package cc.caker.boot.cache;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * JedisTests
 *
 * @author cakeralter
 * @date 2021/8/28
 * @since 1.0
 */
@SpringBootTest
public class JedisTests {

    @Test
    public void testJedisCluster() {
        Set<HostAndPort> nodes = new HashSet<>(2);
        nodes.add(new HostAndPort("81.69.255.167", 6379));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(4);

        JedisCluster jedisCluster = new JedisCluster(
                nodes,
                30,
                30,
                5,
                "redispassword",
                poolConfig);

        jedisCluster.setex("test-connection", 3600 * 24, "1");

        jedisCluster.close();
    }

    @Test
    public void testJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(4);

        JedisPool jedisPool = new JedisPool(
                poolConfig,
                "81.69.255.167",
                6379,
                30,
                "redispassword");

        Jedis jedis = jedisPool.getResource();
        jedis.setex("test-connection", 300, "1");

        jedis.close();
        jedisPool.close();
    }
}
