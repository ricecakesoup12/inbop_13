package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_prescriptions")
@Getter
@Setter
public class ExercisePrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;  // 처방 대상 사용자 ID
    
    // 시작 스트레칭 (분)
    @Column(nullable = false)
    private Integer startStretchingMinutes;  // 5, 10, 15 중 선택
    
    @Column(length = 500)
    private String startStretchingUrl;  // 시작 스트레칭 URL (선택사항)
    
    // 인터벌 운동
    @Column(nullable = false)
    private Integer walkingMinutes;  // 걷기 분
    
    @Column(nullable = false)
    private Integer runningMinutes;  // 뛰기 분
    
    @Column(nullable = false)
    private Integer sets;  // 세트 수
    
    // 마무리 스트레칭 (분)
    @Column(nullable = false)
    private Integer endStretchingMinutes;  // 5, 10, 15 중 선택
    
    @Column(length = 500)
    private String endStretchingUrl;  // 마무리 스트레칭 URL (선택사항)
    
    @Column(nullable = false)
    private String status;  // PENDING, ACCEPTED, DECLINED, COMPLETED
    
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}

