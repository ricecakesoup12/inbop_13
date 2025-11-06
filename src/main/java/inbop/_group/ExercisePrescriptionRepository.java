package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExercisePrescriptionRepository extends JpaRepository<ExercisePrescription, Long> {
    /**
     * 특정 사용자의 처방 조회 (시간순 정렬)
     */
    List<ExercisePrescription> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 특정 사용자의 특정 상태 처방 조회 (시간순 정렬) - 가장 최근 것 반환
     */
    List<ExercisePrescription> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    /**
     * 특정 사용자의 대기 중인 처방 조회 (하나만)
     */
    Optional<ExercisePrescription> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    /**
     * 모든 처방 조회 (시간순 정렬)
     */
    List<ExercisePrescription> findAllByOrderByCreatedAtDesc();
}

