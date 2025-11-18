package inbop._group;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SurveyService {

    private final Map<String, SurveyCreateRequestDto> surveyData = new HashMap<>();
    private final StretchGenerator stretchGenerator;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    public SurveyService(StretchGenerator stretchGenerator, 
                        SurveyRepository surveyRepository,
                        UserRepository userRepository) {
        this.stretchGenerator = stretchGenerator;
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
    }

    public String createSurvey(SurveyCreateRequestDto req) {
        String id = "survey_" + System.currentTimeMillis();
        surveyData.put(id, req);
        return id;
    }

    public Map<String, Object> generateRoutine(String surveyId) {
        SurveyCreateRequestDto survey = surveyData.get(surveyId);
        
        if (survey == null) {
            return Map.of("error", "설문 데이터를 찾을 수 없습니다.");
        }
        
        // 설문에서 사용자 정보 가져오기
        String userName = survey.getName();
        
        // userName으로 사용자 찾기
        User user = null;
        if (userName != null && !userName.isEmpty()) {
            user = userRepository.findByName(userName).stream().findFirst().orElse(null);
        }
        
        // 사용자의 userCode를 사용하여 AI 추천 받기
        if (user != null && user.getUserCode() != null) {
            return stretchGenerator.recommendStretchingByUserCode(user.getUserCode());
        }
        
        // userCode가 없으면 기본 메시지
        return Map.of(
            "message", "AI 운동 추천을 받으려면 사용자 등록이 필요합니다.",
            "설문데이터", survey
        );
    }
}
