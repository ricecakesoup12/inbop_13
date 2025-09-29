package inbop._group;

//JSON 자바 객체로 담아두기 위함

import inbop._group.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SurveyController {
    private final SurveyService service;

    @PostMapping("/api/surveys")    //API 주소
    public String createSurvey(@RequestBody SurveyCreateRequestDto requestDto) {
        //서비스에 DTO 넘겨주기
        service.createSurvey(requestDto);

        return "설문 제출 완료!";

    }
}
