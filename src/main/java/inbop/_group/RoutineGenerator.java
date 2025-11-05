package inbop._group;

import java.util.Map;

public interface RoutineGenerator {
    Map<String,Object> generate(SurveyCreateRequestDto survey);
}
