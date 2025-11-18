package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_requests")
@Getter
@Setter
public class SurveyRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;  // 설문 대상 사용자 ID
    private String message;  // 설문 요청 메시지
    
    @Column(nullable = false)
    private String status;  // PENDING, COMPLETED, DECLINED
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}


