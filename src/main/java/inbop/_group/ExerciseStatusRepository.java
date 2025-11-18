package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExerciseStatusRepository extends JpaRepository<ExerciseStatus, Long> {
    Optional<ExerciseStatus> findByUserId(Long userId);
}


