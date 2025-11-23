package inbop._group.heart_rate_alerts_api.Dto;

import lombok.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 경고 요청

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateHeartRateAlertRequest {

    @org.jetbrains.annotations.NotNull
    private Long userId;

    @NotNull
    @Min(0)
    private Integer heartRate;

    @NotBlank
    private String alertType;    // "warning" 만 허용
}