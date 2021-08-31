package cc.caker.boot.web.test;

import cc.caker.boot.infrastructure.cache.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BloomTests
 *
 * @author cakeralter
 * @date 2021/8/31
 * @since 1.0
 */
@RequiredArgsConstructor
@RequestMapping("/tests/bloom")
@RestController
public class BloomTests {

    private final RedisService redisService;

    @GetMapping("/create")
    public Object create(String key) {
        redisService.bfreserve(key, 100L, 0.01);
        return true;
    }

    @GetMapping("/add")
    public Object add(String key, String[] values) {
        redisService.bfmadd(key, values);
        return true;
    }

    @GetMapping("/exists")
    public Object exists(String key, String value) {
        return redisService.bfexists(key, value);
    }
}
