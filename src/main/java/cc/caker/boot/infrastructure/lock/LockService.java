package cc.caker.boot.infrastructure.lock;

import cc.caker.boot.infrastructure.cache.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * LockService
 *
 * @author cakeralter
 * @date 2021/8/28
 * @since 1.0
 */
@Slf4j
@Component
public class LockService {

    private static final int MAX_RETRY = 3;
    private static final int EXPIRE_3S = 3;
    private static final int TIMEOUT = 10000;
    private static final String LUA_DEL_SCRIPT;
    private static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    static {
        LUA_DEL_SCRIPT = "if redis.call(\"get\", KEYS[1]) == ARGV[1] " +
                "then " +
                "return redis.call(\"del\", KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
    }

    @Autowired
    private RedisService redisService;

    /**
     * lock
     *
     * @param key
     * @return
     */
    public Boolean lock(String key) {
        return tryLock(key, EXPIRE_3S);
    }

    /**
     * lock
     *
     * @param key
     * @param expire
     * @return
     */
    public Boolean lock(String key, Integer expire) {
        return tryLock(key, expire);
    }

    /**
     * tryLock
     *
     * @param key
     * @param expire
     * @return
     */
    public Boolean tryLock(String key, Integer expire) {
        String localVal = threadLocal.get();
        if (StringUtils.isNotBlank(localVal)) {
            String value = redisService.get(key);
            if (localVal.equals(value)) {
                return true;
            }
        }

        long start = System.currentTimeMillis();
        for (; ; ) {
            String result = redisService.setne(key, localVal, expire);
            if ("OK".equals(result)) {
                threadLocal.set(localVal);
                log.info("LockService::tryLock successful, key={}, value={}", key, localVal);
                return true;
            }

            // 判断是否超时
            long interval = System.currentTimeMillis() - start;
            if (interval > TIMEOUT) {
                log.warn("LockService::tryLock timeout, key={}", key);
                return false;
            }

            try {
                // 等待100ms重试
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * unlock
     *
     * @param key
     * @return
     */
    public Boolean unlock(String key) {
        String localVal = threadLocal.get();
        if (StringUtils.isBlank(localVal)) {
            log.warn("LockService::unlock failed, key={}", key);
            return false;
        }
        Boolean result = redisService.eval(
                LUA_DEL_SCRIPT,
                Collections.singletonList(key),
                Collections.singletonList(localVal));
        if (result) {
            log.info("LockService::unlock successful, key={}, value={}", key, localVal);
            threadLocal.remove();
            return true;
        }
        log.warn("LockService::unlock failed, key={}, value={}", key, localVal);
        return false;
    }
}
