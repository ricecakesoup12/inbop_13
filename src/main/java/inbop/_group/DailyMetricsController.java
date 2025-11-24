package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
    "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"})
public class DailyMetricsController {
    
    @Autowired
    private VitalRecordRepository vitalRecordRepository;
    
    /**
     * 사용자별 일일 메트릭스 조회
     * GET /users/{userId}/metrics/daily?days=7
     */
    @GetMapping("/{userId}/metrics/daily")
    public ResponseEntity<List<DailyMetricsResponse>> getDailyMetrics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days
    ) {
        try {
            // 최근 N일 전부터 오늘까지의 데이터 조회
            LocalDateTime startDate = LocalDateTime.of(
                LocalDate.now().minusDays(days - 1),
                LocalTime.MIN
            );
            
            // 날짜별 평균 심박수 조회 (에러 처리 추가)
            List<VitalRecordRepository.DailyAvgHeartRate> avgHeartRates;
            try {
                avgHeartRates = vitalRecordRepository.findDailyAvgHeartRate(userId, startDate);
            } catch (Exception e) {
                System.err.println("❌ 평균 심박수 조회 실패: " + e.getMessage());
                e.printStackTrace();
                avgHeartRates = new ArrayList<>(); // 빈 리스트 반환
            }
            
            // 날짜별로 Map 생성
            Map<LocalDate, Double> avgHrByDate = avgHeartRates.stream()
                .collect(Collectors.toMap(
                    VitalRecordRepository.DailyAvgHeartRate::getDate,
                    VitalRecordRepository.DailyAvgHeartRate::getAvgHr
                ));
            
            // 최근 N일 데이터 생성 (데이터가 없는 날도 포함)
            List<DailyMetricsResponse> result = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                Double avgHr = avgHrByDate.get(date);
                
                DailyMetricsResponse response = new DailyMetricsResponse();
                response.setDate(date.toString()); // "YYYY-MM-DD" 형식
                response.setAvgHr(avgHr != null ? avgHr.intValue() : null);
                response.setWeight(null); // 몸무게는 별도 API에서 조회
                response.setActivity(null); // 활동량은 별도 계산 필요
                
                result.add(response);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Daily Metrics API 에러: " + e.getMessage());
            e.printStackTrace();
            // 에러 발생 시 최소한 빈 리스트라도 반환 (날짜는 포함)
            List<DailyMetricsResponse> result = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                DailyMetricsResponse response = new DailyMetricsResponse();
                response.setDate(date.toString());
                response.setAvgHr(null);
                response.setWeight(null);
                response.setActivity(null);
                result.add(response);
            }
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * DailyMetrics Response DTO
     */
    public static class DailyMetricsResponse {
        private String date;
        private Integer weight;
        private Integer avgHr;
        private Integer activity;
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public Integer getWeight() {
            return weight;
        }
        
        public void setWeight(Integer weight) {
            this.weight = weight;
        }
        
        public Integer getAvgHr() {
            return avgHr;
        }
        
        public void setAvgHr(Integer avgHr) {
            this.avgHr = avgHr;
        }
        
        public Integer getActivity() {
            return activity;
        }
        
        public void setActivity(Integer activity) {
            this.activity = activity;
        }
    }
}

