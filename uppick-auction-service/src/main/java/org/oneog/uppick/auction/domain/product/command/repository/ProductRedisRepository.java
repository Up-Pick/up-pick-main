package org.oneog.uppick.auction.domain.product.command.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void increment(String key) {

        redisTemplate.opsForValue().increment(key);
    }

}
