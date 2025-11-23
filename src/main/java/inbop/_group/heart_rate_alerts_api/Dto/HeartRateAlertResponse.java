package inbop._group.heart_rate_alerts_api.Dto;

import lombok.*;

import java.time.LocalDateTime;

//경고 단건

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeartRateAlertResponse {
    private Long id;
    private Long userId;
    private Integer heartRate;
    private String alertType;    // "warning"
    private LocalDateTime alertedAt;
}
