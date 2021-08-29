package cc.caker.boot.infrastructure.cache;

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
 * RedisUtils
 *
 * @author cakeralter
 * @date 2021/8/28
 * @since 1.0
 */
@Slf4j
@Component
public class RedisService {

    /**
     * 常量参数
     */
    private static final String HOST = "81.69.255.167";
    private static final String PASSWORD = "redispassword";
    private static final int PORT = 6379;
    private static final int TIMEOUT = 30;

    private static final int MAX_TOTAL = 8;
    private static final int MAX_IDLE = 8;
    private static final int MIN_IDLE = 4;

    private static final Object LOCK = new Object();

    private volatile JedisPool jedisPool;

    /*@PostConstruct
    public void init() {
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
    }*/

    /**
     * set
     *
     * @param key
     * @param value
     */
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
     * 获取连接
     *
     * @return
     */
    private Jedis getResource() {
        if (jedisPool == null) {
            synchronized (LOCK) {
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
