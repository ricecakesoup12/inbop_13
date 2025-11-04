package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/weight-records")
@CrossOrigin(origins = "*")
public class WeightRecordController {
    
    @Autowired
    private WeightRecordRepository weightRecordRepository;
    
    // 사용자의 몸무게 기록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WeightRecord>> getUserWeightRecords(@PathVariable Long userId) {
        List<WeightRecord> records = weightRecordRepository.findByUserIdOrderByRecordDateDesc(userId);
        return ResponseEntity.ok(records);
    }
    
    // 기간별 몸무게 기록 조회
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<WeightRecord>> getUserWeightRecordsByRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<WeightRecord> records = weightRecordRepository.findByUserIdAndRecordDateBetween(userId, start, end);
        return ResponseEntity.ok(records);
    }
}


