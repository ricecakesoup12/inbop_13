package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "surveys")
@Getter
@Setter
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;  // 설문 작성 사용자 ID
    private Long requestId;  // 설문 요청 ID
    
    // 설문 내용 (JSON 형태로 저장)
    @Column(length = 10000)
    private String surveyData;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


