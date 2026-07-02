package com.example.consumer.consumer;

import com.example.consumer.domain.Coupon;
import com.example.consumer.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreateConsumer {  // Topic에 전송된 데이터를 가져오기위한 Consumer 작업

    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon_create", groupId = "group_1")  // "coupon_create" topic에 전송된 데이터를 가져오기 위한 Listener 정의
    public void listener(Long userId) {
        couponRepository.save(new Coupon(userId));
    }
}
