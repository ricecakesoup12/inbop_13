package inbop._group.heart_rate_alerts_api;

// package com.example.heart.alert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "heart_rate_alerts")
@Getter
@Setter
public class HeartRateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 엔티티랑 연관관계 맺고 싶으면 여기 Long 대신 @ManyToOne으로 바꿔도 됨
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer heartRate;

    @Column(nullable = false, length = 20)
    private String alertType;  // "warning"

    @Column(nullable = false)
    private LocalDateTime alertedAt;

    @PrePersist
    protected void onCreate() {
        if (alertedAt == null) {
            alertedAt = LocalDateTime.now();
        }
    }
}
