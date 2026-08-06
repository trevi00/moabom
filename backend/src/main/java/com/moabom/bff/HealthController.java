package com.moabom.bff;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * D-02 단일 진입 오리진의 실동작 증거.
 * 브라우저는 :3000 만 보고, Next rewrites 가 /api/* 를 이 앱으로 프록시한다.
 * (Actuator 의 /actuator/health 와 별개 — 이쪽은 rewrites 경로를 통과하는지 자체를 증명한다.)
 */
@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    Map<String, Object> health() {
        return Map.of("status", "UP", "app", "moabom", "ts", Instant.now().toString());
    }
}
