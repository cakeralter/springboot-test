package cc.caker.boot.infrastructure.cache;

import java.util.List;

/**
 * RedisService
 * Redis操作封装
 *
 * @author cakeralter
 * @date 2021/8/31
 * @since 1.0
 */
public interface RedisService {

    /**
     * set
     *
     * @param key
     * @param value
     */
    void set(String key, String value);

    /**
     * get
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * set nx ex
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    String setne(String key, String value, Integer expire);

    /**
     * del
     *
     * @param key
     */
    void del(String key);

    /**
     * del
     *
     * @param script
     * @param keys
     * @param args
     * @return
     */
    Boolean eval(String script, List<String> keys, List<String> args);

    /**
     * 新建一个bloom
     *
     * @param key  bloom key
     * @param cap  初始容量
     * @param rate 重复率
     */
    void bfreserve(String key, Long cap, double rate);

    /**
     * bfmadd
     *
     * @param key
     * @param values
     */
    void bfmadd(String key, String... values);

    /**
     * bfexists
     *
     * @param key
     * @param value
     * @return
     */
    Boolean bfexists(String key, String value);
}
