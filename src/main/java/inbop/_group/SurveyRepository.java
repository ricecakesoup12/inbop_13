package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByUserId(Long userId);
    Optional<Survey> findTopByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Survey> findByRequestId(Long requestId);
}

