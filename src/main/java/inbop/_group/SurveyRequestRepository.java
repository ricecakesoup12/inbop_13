package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SurveyRequestRepository extends JpaRepository<SurveyRequest, Long> {
    List<SurveyRequest> findByUserIdAndStatus(Long userId, String status);
    List<SurveyRequest> findByUserId(Long userId);
}

