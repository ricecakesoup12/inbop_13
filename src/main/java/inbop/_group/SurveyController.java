package inbop._group;

//JSON 자바 객체로 담아두기 위함

import inbop._group.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:3001")
public class SurveyController {
    private final SurveyService service;

    @PostMapping("/api/surveys")    //API 주소
    public ResponseEntity<?> createSurvey(@RequestBody SurveyCreateRequestDto requestDto) {

        service.createSurvey(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "설문이 성공적으로 제출되었습니다."));
    }
}
