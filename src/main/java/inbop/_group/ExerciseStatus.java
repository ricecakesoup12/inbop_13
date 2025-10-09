package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_status")
@Getter
@Setter
public class ExerciseStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private Long userId;
    
    private Boolean isExercising;  // 운동 중 여부
    private LocalDateTime startedAt;  // 운동 시작 시간
    private LocalDateTime updatedAt;  // 마지막 업데이트 시간
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

