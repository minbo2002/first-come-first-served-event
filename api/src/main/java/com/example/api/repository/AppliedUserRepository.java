package com.example.api.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppliedUserRepository {  // 1인당 1개 쿠폰 제한을 위한 Redis Set 자료구조 활용

    private final RedisTemplate<String, String> redisTemplate;

    public Long add(Long userId) {
        return redisTemplate
                    .opsForSet()
                    .add("applied_user", userId.toString());
    }
}
