package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService  applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
        redisTemplate.delete("coupon_count");
    }

    // 1명 응모
    @Test
    public void one_time_entry() {
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1L);
    }

    // 1000명이 멀티쓰레드 환경에서 동시에 응모(race condition 발생)
    @Test
    public void one_thousand_simultaneous_entries() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // ExecutorService 병렬 작업 간단하게 할 수 있는 JAVA api
        CountDownLatch latch = new CountDownLatch(threadCount);  // 다른 스레드에서 수행하는 작업을 기다리도록 도와주는 클래스

        for(int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(10000);  // 10초 대기 (Kafka consumer가 비동기적으로 동작하기 때문에, Kafka consumer가 Coupon을 저장할 시간을 주기 위해서 10초 대기)

        long count = couponRepository.count();

        assertThat(count).isEqualTo(100L);  // 1000명이 동시에 응모했지만, 최대 100명만 당첨되도록 제한
    }

    // userId=1 유저가 1000번 요청 보냈을때 1개의 쿠폰만 발급되는지 테스트
    @Test
    public void one_coupon_issued_per_person() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32); // ExecutorService 병렬 작업 간단하게 할 수 있는 JAVA api
        CountDownLatch latch = new CountDownLatch(threadCount);  // 다른 스레드에서 수행하는 작업을 기다리도록 도와주는 클래스

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(10000);  // 10초 대기 (Kafka consumer가 비동기적으로 동작하기 때문에, Kafka consumer가 Coupon을 저장할 시간을 주기 위해서 10초 대기)

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);  // 1000명이 동시에 응모했지만, 최대 100명만 당첨되도록 제한
    }

}