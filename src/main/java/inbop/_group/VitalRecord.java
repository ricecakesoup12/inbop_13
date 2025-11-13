package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_records")
@Getter
@Setter
public class VitalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;  // 사용자 ID
    
    @Column(nullable = false)
    private Integer hr;  // 심박수 (bpm)
    
    @Column(nullable = false)
    private Integer spo2;  // 산소포화도 (%)
    
    @Column(nullable = false)
    private LocalDateTime recordedAt;  // 기록 시간
    
    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}



