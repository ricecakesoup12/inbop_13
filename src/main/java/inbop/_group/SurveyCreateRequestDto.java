package inbop._group;

//설문 목록

import lombok.Getter;
import java.util.List;

@Getter
public class SurveyCreateRequestDto {
    //1. 기본 정보
    private String gender;
    private int age;
    private double height;
    private double weight;
    private String pattern;

    //2. 건강 상태
    private List<String> diseases;
    private boolean recentSurgery;
    private String medicationDetails;
    private String sleepHours;

    //3. 생활 습관
    private String smoking;
    private String drinking;
    private String dietHabit;

    //4. 운동 경험 및 체력
    private String weeklyExercise;
    private List<String> preferredExercise;
    private List<String> exerciseGoal;
    private int fitnessLevel;

    //5. 신체적 한계 및 특이사항
    private List<String> painArea;
    private boolean exerciseRestriction;

    //6. 심리/동기 요인
    private List<String> motivation;
    private String confidence;
    private String preferredEnvironment;
}
