package inbop._group;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SurveyService {

    private final Map<String, SurveyCreateRequestDto> surveyData = new HashMap<>();
    private final RoutineGenerator generator;

    public SurveyService(RoutineGenerator generator) {
        this.generator = generator;
    }

    public String createSurvey(SurveyCreateRequestDto req) {
        String id = "survey_" + System.currentTimeMillis();
        surveyData.put(id, req);
        return id;
    }

    public Map<String, Object> generateRoutine(String surveyId) {
        SurveyCreateRequestDto survey = surveyData.get(surveyId);
        return generator.generate(survey);
    }
}
