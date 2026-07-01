package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    // Redis를 incr 을 활용하여 race condition 방지
    public void apply(Long userId) {
        Long count = couponCountRepository.increment();

        if(count > 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }

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
