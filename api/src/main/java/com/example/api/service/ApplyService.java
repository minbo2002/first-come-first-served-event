package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;
    private final AppliedUserRepository appliedUserRepository;

    // Redis를 incr 을 활용하여 race condition 방지
    public void apply(Long userId) {
        Long apply = appliedUserRepository.add(userId);// 1인당 1개 쿠폰 제한을 위한 Redis Set 자료구조 활용

        if(apply != 1) {  // 이미 발급 요청을 했을때
            return;
        }

        Long count = couponCountRepository.increment();

        if(count > 100) {
            return;
        }

        couponCreateProducer.create(userId);
    }

    /**
    // Redis를 incr 을 활용하여 race condition 방지
    public void apply(Long userId) {
        Long count = couponCountRepository.increment();

        if(count > 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }*/

    /**
    public void apply(Long userId) {
        long count = couponRepository.count();

        if(count > 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
     */
}
