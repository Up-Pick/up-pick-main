package org.oneog.uppick.auction;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class UppickProductApplicationTest {

    @MockitoBean
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {

    }

}
