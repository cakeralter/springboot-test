package cc.caker.boot.infrastructure.lock;

import cc.caker.boot.infrastructure.cache.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

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

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 3;
    /**
     * 过期时间3s
     */
    private static final int EXPIRE_3S = 3;
    /**
     * 重试时间10s
     */
    private static final int TIMEOUT = 10000;
    /**
     * 删除锁的Lua脚本
     */
    private static final String LUA_DEL_SCRIPT;
    static {
        LUA_DEL_SCRIPT = "if redis.call(\"get\", KEYS[1]) == ARGV[1] " +
                "then " +
                "return redis.call(\"del\", KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
    }

    /**
     * 记录线程UID 避免锁被其它误删
     */
    private static ThreadLocal<String> threadLocal =
            ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

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
        String threadId = threadLocal.get();
        String value = redisService.get(key);
        if (Objects.equals(threadId, value)) {
            return true;
        }

        long start = System.currentTimeMillis();
        for (; ; ) {
            String result = redisService.setne(key, threadId, expire);
            if ("OK".equals(result)) {
                log.info("LockService::tryLock successful, key={}, value={}", key, threadId);
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
        String threadId = threadLocal.get();
        Boolean result = redisService.eval(
                LUA_DEL_SCRIPT,
                Collections.singletonList(key),
                Collections.singletonList(threadId));
        if (result) {
            log.info("LockService::unlock successful, key={}, value={}", key, threadId);
            return true;
        }

        log.warn("LockService::unlock failed, key={}, value={}", key, threadId);
        return false;
    }
}
