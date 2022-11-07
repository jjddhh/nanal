package com.example.oauthjwt.test;

import com.example.oauthjwt.entity.MemberRepository;
import com.example.oauthjwt.token.Token;
import com.example.oauthjwt.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TestController {

    @Value("${jwt.token.refresh-token-expire-length}")
    private long refresh_token_expire_time;

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/main")
    public String main() {
        log.info("hi");
        return "index.html";
    }

    @ResponseBody
    @GetMapping("/auth")
    public String auth() {
        log.info("hi auth user");
        return "auth success";
    }

    @ResponseBody
    @GetMapping("/redis/test")
    public String store() {

        // given
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "why";

        valueOperations.set(key, "hello");

        return "ok";
    }
}
