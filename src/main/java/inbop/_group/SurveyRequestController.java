package inbop._group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/survey-requests")
@CrossOrigin(origins = "*")
public class SurveyRequestController {
    
    @Autowired
    private SurveyRequestRepository surveyRequestRepository;
    
    @Autowired
    private SurveyRepository surveyRepository;
    
    @Autowired
    private WeightRecordRepository weightRecordRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 설문 요청 보내기
    @PostMapping
    public ResponseEntity<SurveyRequest> createSurveyRequest(@RequestBody Map<String, Object> request) {
        SurveyRequest surveyRequest = new SurveyRequest();
        surveyRequest.setUserId(Long.parseLong(request.get("userId").toString()));
        surveyRequest.setMessage(request.get("message").toString());
        surveyRequest.setStatus("PENDING");
        
        SurveyRequest saved = surveyRequestRepository.save(surveyRequest);
        return ResponseEntity.ok(saved);
    }
    
    // 사용자의 대기 중인 설문 요청 조회
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<SurveyRequest>> getPendingSurveyRequests(@PathVariable Long userId) {
        List<SurveyRequest> requests = surveyRequestRepository.findByUserIdAndStatus(userId, "PENDING");
        return ResponseEntity.ok(requests);
    }
    
    // 사용자의 모든 설문 요청 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SurveyRequest>> getUserSurveyRequests(@PathVariable Long userId) {
        List<SurveyRequest> requests = surveyRequestRepository.findByUserId(userId);
        return ResponseEntity.ok(requests);
    }
    
    // 설문 제출 (사용자가 설문 작성 완료)
    @PostMapping("/{requestId}/submit")
    public ResponseEntity<Survey> submitSurvey(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> surveyData) {
        
        try {
            // 설문 요청 상태 업데이트
            SurveyRequest request = surveyRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("설문 요청을 찾을 수 없습니다."));
            
            request.setStatus("COMPLETED");
            request.setCompletedAt(LocalDateTime.now());
            surveyRequestRepository.save(request);
            
            // 설문 결과를 JSON 문자열로 변환
            String surveyDataJson = objectMapper.writeValueAsString(surveyData);
            
            // 설문 결과 저장
            Survey survey = new Survey();
            survey.setUserId(request.getUserId());
            survey.setRequestId(requestId);
            survey.setSurveyData(surveyDataJson);
            
            Survey saved = surveyRepository.save(survey);
            
            // 몸무게 데이터 별도 저장 (그래프용)
            if (surveyData.containsKey("weight") && surveyData.get("weight") != null) {
                WeightRecord weightRecord = new WeightRecord();
                weightRecord.setUserId(request.getUserId());
                weightRecord.setWeight(Double.parseDouble(surveyData.get("weight").toString()));
                weightRecordRepository.save(weightRecord);
            }
            
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            throw new RuntimeException("설문 제출 실패: " + e.getMessage());
        }
    }
    
    // 설문 결과 조회 (최신)
    @GetMapping("/user/{userId}/latest-result")
    public ResponseEntity<Survey> getLatestSurveyResult(@PathVariable Long userId) {
        return surveyRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 모든 설문 결과 조회
    @GetMapping("/user/{userId}/all-results")
    public ResponseEntity<List<Survey>> getAllSurveyResults(@PathVariable Long userId) {
        List<Survey> surveys = surveyRepository.findByUserId(userId);
        return ResponseEntity.ok(surveys);
    }
    
    // 특정 설문 조회 (ID로)
    @GetMapping("/result/{surveyId}")
    public ResponseEntity<Survey> getSurveyById(@PathVariable Long surveyId) {
        return surveyRepository.findById(surveyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 특정 설문 요청의 결과 조회
    @GetMapping("/{requestId}/result")
    public ResponseEntity<Survey> getSurveyResult(@PathVariable Long requestId) {
        return surveyRepository.findByRequestId(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

