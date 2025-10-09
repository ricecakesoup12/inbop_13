package inbop._group;

//

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SurveyService {
    
    // 설문 데이터를 임시 저장할 Map (실제로는 DB 사용)
    private Map<String, SurveyCreateRequestDto> surveyData = new HashMap<>();
    
    public String createSurvey(SurveyCreateRequestDto requestDto) {
        //받은 설문 DB에 저장
        String surveyId = "survey_" + System.currentTimeMillis();
        surveyData.put(surveyId, requestDto);
        
        return surveyId;
    }
    
    public Map<String, Object> generateRoutine(String surveyId) {
        // 설문 데이터 조회
        SurveyCreateRequestDto survey = surveyData.get(surveyId);
        
        if (survey == null) {
            // 설문 데이터가 없는 경우 기본 루틴 반환
            return getDefaultRoutine();
        }
        
        // 설문 결과를 바탕으로 맞춤 루틴 생성
        return createCustomRoutine(survey);
    }
    
    private Map<String, Object> createCustomRoutine(SurveyCreateRequestDto survey) {
        Map<String, Object> routine = new HashMap<>();
        
        // 기본 정보
        routine.put("userName", survey.getName() != null ? survey.getName() : "사용자");
        routine.put("age", survey.getAge() != null ? survey.getAge() : 30);
        routine.put("fitnessLevel", survey.getFitnessLevel() != null ? survey.getFitnessLevel().toString() : "3");
        
        // 운동 목표 설정
        List<String> goals = survey.getExerciseGoal();
        String primaryGoal = (goals != null && !goals.isEmpty()) ? goals.get(0) : "건강 관리";
        routine.put("exerciseGoal", primaryGoal);
        
        // 선호 운동 설정
        List<String> preferred = survey.getPreferredExercise();
        String preferredExercise = (preferred != null && !preferred.isEmpty()) ? 
            String.join(", ", preferred) : "유산소, 근력";
        routine.put("preferredExercise", preferredExercise);
        
        // 체력 수준에 따른 운동 강도 및 빈도 설정
        int fitnessLevel = survey.getFitnessLevel() != null ? survey.getFitnessLevel() : 3;
        
        if (fitnessLevel <= 2) {
            routine.put("weeklyFrequency", 2);
            routine.put("sessionDuration", 30);
            routine.put("intensity", "낮음");
        } else if (fitnessLevel == 3) {
            routine.put("weeklyFrequency", 3);
            routine.put("sessionDuration", 45);
            routine.put("intensity", "보통");
        } else {
            routine.put("weeklyFrequency", 4);
            routine.put("sessionDuration", 60);
            routine.put("intensity", "높음");
        }
        
        // 주간 운동 계획 생성
        routine.put("weeklyPlan", generateWeeklyPlan(fitnessLevel, preferred));
        
        // 맞춤 팁 생성
        routine.put("tips", generateTips(survey));
        
        return routine;
    }
    
    private List<Map<String, Object>> generateWeeklyPlan(int fitnessLevel, List<String> preferred) {
        List<Map<String, Object>> weeklyPlan = new ArrayList<>();
        String[] days = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("dayName", days[i]);
            
            // 체력 수준과 요일에 따른 운동 계획
            if (fitnessLevel <= 2) {
                // 초보자: 월, 수, 금 운동
                if (i == 0 || i == 2 || i == 4) {
                    dayPlan.put("type", "가벼운 운동");
                    dayPlan.put("exercises", getBeginnerExercises());
                } else {
                    dayPlan.put("type", "휴식");
                    dayPlan.put("exercises", new ArrayList<>());
                }
            } else if (fitnessLevel == 3) {
                // 중급자: 월, 수, 금 운동
                if (i == 0) {
                    dayPlan.put("type", "유산소");
                    dayPlan.put("exercises", getCardioExercises());
                } else if (i == 2) {
                    dayPlan.put("type", "근력");
                    dayPlan.put("exercises", getStrengthExercises());
                } else if (i == 4) {
                    dayPlan.put("type", "유산소");
                    dayPlan.put("exercises", getCardioExercises());
                } else {
                    dayPlan.put("type", "휴식");
                    dayPlan.put("exercises", new ArrayList<>());
                }
            } else {
                // 고급자: 월, 화, 목, 금 운동
                if (i == 0 || i == 4) {
                    dayPlan.put("type", "유산소");
                    dayPlan.put("exercises", getAdvancedCardioExercises());
                } else if (i == 1 || i == 3) {
                    dayPlan.put("type", "근력");
                    dayPlan.put("exercises", getAdvancedStrengthExercises());
                } else {
                    dayPlan.put("type", "휴식");
                    dayPlan.put("exercises", new ArrayList<>());
                }
            }
            
            weeklyPlan.add(dayPlan);
        }
        
        return weeklyPlan;
    }
    
    private List<Map<String, Object>> getBeginnerExercises() {
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(Map.of("name", "걷기", "sets", 1, "reps", "20분"));
        exercises.add(Map.of("name", "스트레칭", "sets", 1, "reps", "10분"));
        return exercises;
    }
    
    private List<Map<String, Object>> getCardioExercises() {
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(Map.of("name", "빠르게 걷기", "sets", 1, "reps", "30분"));
        exercises.add(Map.of("name", "스트레칭", "sets", 1, "reps", "10분"));
        return exercises;
    }
    
    private List<Map<String, Object>> getStrengthExercises() {
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(Map.of("name", "스쿼트", "sets", 3, "reps", 15));
        exercises.add(Map.of("name", "팔굽혀펴기", "sets", 3, "reps", 10));
        exercises.add(Map.of("name", "플랭크", "sets", 3, "reps", "30초"));
        return exercises;
    }
    
    private List<Map<String, Object>> getAdvancedCardioExercises() {
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(Map.of("name", "달리기", "sets", 1, "reps", "40분"));
        exercises.add(Map.of("name", "쿨다운", "sets", 1, "reps", "10분"));
        return exercises;
    }
    
    private List<Map<String, Object>> getAdvancedStrengthExercises() {
        List<Map<String, Object>> exercises = new ArrayList<>();
        exercises.add(Map.of("name", "스쿼트", "sets", 4, "reps", 20));
        exercises.add(Map.of("name", "팔굽혀펴기", "sets", 4, "reps", 15));
        exercises.add(Map.of("name", "플랭크", "sets", 4, "reps", "60초"));
        exercises.add(Map.of("name", "버피", "sets", 3, "reps", 10));
        return exercises;
    }
    
    private List<String> generateTips(SurveyCreateRequestDto survey) {
        List<String> tips = new ArrayList<>();
        
        // 기본 팁
        tips.add("운동 전후 충분한 스트레칭을 해주세요");
        tips.add("운동 중 수분 섭취를 잊지 마세요");
        tips.add("무리하지 말고 본인의 페이스에 맞춰 진행하세요");
        
        // 건강 상태에 따른 맞춤 팁
        List<String> diseases = survey.getDiseases();
        if (diseases != null && diseases.contains("고혈압")) {
            tips.add("고혈압이 있으시니 운동 강도를 천천히 높여가세요");
        }
        if (diseases != null && diseases.contains("관절질환")) {
            tips.add("관절에 무리가 가지 않는 저충격 운동을 선택하세요");
        }
        
        // 나이에 따른 팁
        if (survey.getAge() != null && survey.getAge() >= 50) {
            tips.add("균형감각 향상을 위한 운동도 포함해보세요");
        }
        
        tips.add("규칙적인 운동이 가장 중요합니다");
        
        return tips;
    }
    
    private Map<String, Object> getDefaultRoutine() {
        Map<String, Object> routine = new HashMap<>();
        routine.put("userName", "사용자");
        routine.put("age", 30);
        routine.put("fitnessLevel", "3");
        routine.put("exerciseGoal", "건강 관리");
        routine.put("preferredExercise", "유산소, 근력");
        routine.put("weeklyFrequency", 3);
        routine.put("sessionDuration", 45);
        routine.put("intensity", "보통");
        routine.put("weeklyPlan", generateWeeklyPlan(3, Arrays.asList("유산소", "근력")));
        routine.put("tips", Arrays.asList(
            "운동 전후 충분한 스트레칭을 해주세요",
            "운동 중 수분 섭취를 잊지 마세요",
            "무리하지 말고 본인의 페이스에 맞춰 진행하세요",
            "규칙적인 운동이 가장 중요합니다"
        ));
        return routine;
    }
}
