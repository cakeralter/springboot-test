package cc.caker.boot.web.test;

import cc.caker.boot.infrastructure.lock.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LockTests
 *
 * @author cakeralter
 * @date 2021/8/29
 * @since 1.0
 */
@RequestMapping("/tests/lock")
@RestController
public class LockTests {

    @Autowired
    private LockService lockService;

    @GetMapping
    public Object lock(String key) {
        Boolean result = lockService.lock(key, 30);
        if (result) {
            return "lock successful";
        }
        return "lock fail";
    }

    @GetMapping("/unlock")
    public Object unlock(String key) {
        Boolean result = lockService.unlock(key);
        if (result) {
            return "unlock successful";
        }
        return "unlock fail";
    }
}
