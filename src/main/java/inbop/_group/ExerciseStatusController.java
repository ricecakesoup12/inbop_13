package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/exercise-status")
@CrossOrigin(origins = "*")
public class ExerciseStatusController {
    
    @Autowired
    private ExerciseStatusRepository exerciseStatusRepository;
    
    // 운동 상태 업데이트
    @PostMapping("/user/{userId}")
    public ResponseEntity<ExerciseStatus> updateExerciseStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        
        Boolean isExercising = (Boolean) request.get("isExercising");
        
        ExerciseStatus status = exerciseStatusRepository.findByUserId(userId)
                .orElse(new ExerciseStatus());
        
        status.setUserId(userId);
        status.setIsExercising(isExercising);
        
        if (isExercising) {
            status.setStartedAt(LocalDateTime.now());
        }
        
        ExerciseStatus saved = exerciseStatusRepository.save(status);
        return ResponseEntity.ok(saved);
    }
    
    // 운동 상태 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<ExerciseStatus> getExerciseStatus(@PathVariable Long userId) {
        return exerciseStatusRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(createDefaultStatus(userId)));
    }
    
    private ExerciseStatus createDefaultStatus(Long userId) {
        ExerciseStatus status = new ExerciseStatus();
        status.setUserId(userId);
        status.setIsExercising(false);
        return status;
    }
}

