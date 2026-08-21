package com.example.SSO_project.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis-test")
    @ResponseBody
    public String redisTest(){
        stringRedisTemplate.opsForValue().set("hello", "welcome to the redis");
        String value = stringRedisTemplate.opsForValue().get("hello");
        return "SET + GOT" + value;
    }

}
