package inbop._group.heart_rate_alerts_api.Dto;

import lombok.*;
import java.time.LocalDateTime;

//보호자용 활성 경고 목록

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveAlertResponse {
    private Long userId;
    private String userName;
    private Integer heartRate;
    private LocalDateTime alertedAt;
}