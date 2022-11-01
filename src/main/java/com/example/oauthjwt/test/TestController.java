package com.example.oauthjwt.test;

import com.example.oauthjwt.entity.MemberRepository;
import com.example.oauthjwt.token.Token;
import com.example.oauthjwt.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Controller
public class TestController {

    @Value("${jwt.token.refresh-token-expire-length}")
    private long refresh_token_expire_time;

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/test")
    public String test() {
        System.out.println("here");
        return "index.html";
    }

    @ResponseBody
    @GetMapping("/redis/test")
    public String store() {

        /*redisTemplate.opsForValue().set(
                "test",
                "Exist",
                refresh_token_expire_time,
                TimeUnit.MILLISECONDS
        );*/

        // given
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "why";

        valueOperations.set(key, "hello");

        return "ok";
    }
}
