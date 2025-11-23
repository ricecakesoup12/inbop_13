package inbop._group.heart_rate_alerts_api.Dto;

import lombok.*;

//주간 카운트

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAlertCountResponse {
    private String date;    // "YYYY-MM-DD"
    private Long count;
}