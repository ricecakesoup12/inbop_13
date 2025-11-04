package inbop._group;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RoutineRequest(
        @JsonProperty("목표")
        String goal,
        int daysPerWeek,
        @JsonProperty("강도")
        String level,
        String constraints
) {}

record RoutineItem(
        @JsonProperty("일차")
        int day,
        @JsonProperty("제목")
        String title,
        @JsonProperty("설명")
        String description,
        @JsonProperty("운동 목록")
        String[] exercises,
        @JsonProperty("운동 시간")
        String duration
) {}

record RoutineResponse(
        @JsonProperty("요약")
        String summary,
        @JsonProperty("계획")
        List<RoutineItem> plan
) {}
