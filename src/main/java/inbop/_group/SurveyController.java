package inbop._group;

//JSON 자바 객체로 담아두기 위함

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class SurveyController {
    private final SurveyService service;

    @PostMapping("/api/survey")    //API 주소 (프론트엔드와 맞춤)
    public Map<String, Object> createSurvey(@RequestBody SurveyCreateRequestDto requestDto) {
        //서비스에 DTO 넘겨주기
        String surveyId = service.createSurvey(requestDto);
        
        return Map.of(
            "message", "설문 제출 완료!",
            "id", surveyId,
            "surveyId", surveyId
        );
    }
    
    @GetMapping("/api/routine/{surveyId}")
    public Map<String, Object> getRoutine(@PathVariable String surveyId) {
        // 설문 결과를 바탕으로 맞춤 루틴 생성
        return service.generateRoutine(surveyId);
    }
}
