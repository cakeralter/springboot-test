package cc.caker.boot.infrastructure.cache.impl;

import cc.caker.boot.infrastructure.cache.RedisService;
import com.aliyun.tair.tairbloom.TairBloom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RedisServiceImpl
 *
 * @author cakeralter
 * @date 2021/8/28
 * @since 1.0
 */
@Slf4j
@Component
public class RedisServiceImpl implements RedisService {

    /**
     * 常量参数
     */
    private static final String HOST = "172.18.0.1";
    private static final String PASSWORD = "redispassword";
    private static final int PORT = 6379;
    private static final int TIMEOUT = 30;

    private static final int MAX_TOTAL = 8;
    private static final int MAX_IDLE = 8;
    private static final int MIN_IDLE = 4;

    private volatile JedisPool jedisPool;

    /**
     * set
     *
     * @param key
     * @param value
     */
    @Override
    public void set(String key, String value) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                jedis.set(key, value);
            }
        }
    }

    /**
     * get
     *
     * @param key
     * @return
     */
    @Override
    public String get(String key) {
        try (Jedis jedis = getResource()) {
            return Optional.ofNullable(jedis)
                    .map(c -> c.get(key))
                    .orElse(null);
        }
    }

    /**
     * set nx ex
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    @Override
    public String setne(String key, String value, Integer expire) {
        try (Jedis jedis = getResource()) {
            if (jedis == null) {
                return "-1";
            }
            SetParams params = SetParams.setParams().nx().ex(expire);
            return jedis.set(key, value, params);
        }
    }

    /**
     * del
     *
     * @param key
     */
    @Override
    public void del(String key) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                jedis.del(key);
            }
        }
    }

    /**
     * eval
     *
     * @param script
     * @param keys
     * @param args
     */
    @Override
    public Boolean eval(String script, List<String> keys, List<String> args) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                Object result = jedis.eval(script, keys, args);
                return Objects.equals(1L, result);
            }
        }
        return false;
    }

    /**
     * 新建一个bloom
     *
     * @param key  bloom key
     * @param cap  初始容量
     * @param rate 重复率
     */
    @Override
    public void bfreserve(String key, Long cap, double rate) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                TairBloom tairBloom = new TairBloom(jedis);
                tairBloom.bfreserve(key, cap, rate);
            }
        }
    }

    /**
     * bfmadd
     *
     * @param key
     * @param values
     */
    @Override
    public void bfmadd(String key, String... values) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                TairBloom tairBloom = new TairBloom(jedis);
                tairBloom.bfmadd(key, values);
            }
        }
    }

    /**
     * bfexists
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public Boolean bfexists(String key, String value) {
        try (Jedis jedis = getResource()) {
            if (jedis != null) {
                TairBloom tairBloom = new TairBloom(jedis);
                return tairBloom.bfexists(key, value);
            }
        }
        return false;
    }

    /**
     * 获取连接
     *
     * @return
     */
    private Jedis getResource() {
        if (jedisPool == null) {
            synchronized (RedisServiceImpl.class) {
                if (jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(MAX_TOTAL);
                    poolConfig.setMaxIdle(MAX_IDLE);
                    poolConfig.setMinIdle(MIN_IDLE);

                    // 初始化连接池
                    jedisPool = new JedisPool(
                            poolConfig,
                            HOST,
                            PORT,
                            TIMEOUT,
                            PASSWORD);

                    log.info("JedisPool init...");
                    return jedisPool.getResource();
                }
            }
        }
        return jedisPool.getResource();
    }

    @PreDestroy
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            log.info("JedisPool is close...");
        }
    }
}
