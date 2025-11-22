package inbop._group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.*;

@Service
public class StretchGenerator {

    // ---------- OpenAI 설정 ----------
    @Value("${openai.api.key:#{null}}")
    private String apiKeyFromProps;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.timeout-ms:20000}")
    private int timeoutMs;

    private final ObjectMapper om = new ObjectMapper();

    // ---------- DB ----------
    private final JdbcTemplate jdbc;
    private final ChatMessageRepository chatMessageRepository;

    // ---------- AI system 프롬프트 ----------
    @Value("classpath:ai-prompt.txt")
    private Resource systemPromptResource;

    @Autowired
    public StretchGenerator(JdbcTemplate jdbc,
                            ChatMessageRepository chatMessageRepository) {
        this.jdbc = jdbc;
        this.chatMessageRepository = chatMessageRepository;
    }

    // ===================== 공개 메서드 =====================

    public Map<String, Object> recommendStretchingByUserCode(String userCode) {
        UserProfile p = findUserByCode(userCode);
        return recommendStretchingInternal(p);
    }

    public Map<String, Object> recommendStretchingByUserId(Long userId) {
        UserProfile p = findUserById(userId);
        return recommendStretchingInternal(p);
    }

    // ===================== 메인 로직 =====================

    private Map<String, Object> recommendStretchingInternal(UserProfile p) {
        System.out.println("=== AI 운동 및 공원 추천 시작 ===");

        // 1) 채팅 기록 조회
        String userKey = p.id != null ? String.valueOf(p.id) : p.userCode;
        List<ChatMessage> allMessages = userKey != null
                ? chatMessageRepository.findByUserIdOrderByTimestampAsc(userKey)
                : List.of();
        List<ChatMessage> recent = lastMessages(allMessages, 30);

        String recentText = recent.stream()
                .filter(m -> "user".equalsIgnoreCase(m.getSender()))
                .map(ChatMessage::getMessage)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> "- " + s)
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "\n") + b);

        // 2) 유저 프로필 구성
        String profile = """
                name: %s
                age: %s
                gender: %s
                chronic_diseases: %s
                bmi: %s
                """.formatted(
                opt(p.name, "사용자"),
                p.age == null ? "unknown" : p.age,
                opt(p.gender, "unknown"),
                p.chronicDiseases.isEmpty() ? "none" : String.join(", ", p.chronicDiseases),
                p.bmi == null ? "unknown" : String.format(Locale.US, "%.1f", p.bmi)
        );

        StringBuilder userMsg = new StringBuilder();
        userMsg.append("USER PROFILE:\n").append(profile).append("\n");
        userMsg.append("RECENT USER MESSAGES (KOREAN):\n");
        if (recentText.isBlank()) {
            userMsg.append("- (최근 사용자 메시지가 거의 없습니다.)\n");
        } else {
            userMsg.append(recentText).append("\n");
        }

        // 3) System Prompt 읽기
        String system;
        try (InputStreamReader reader =
                     new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            system = FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new RuntimeException("System 프롬프트를 읽는 데 실패했습니다.", e);
        }

        // 4) OpenAI 요청
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.3);
        body.put("max_tokens", 800);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", userMsg.toString())
        ));

        try {
            String apiKey = resolveApiKey();
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeoutMs);
            factory.setReadTimeout(timeoutMs);
            RestTemplate rt = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<Map> res = rt.exchange(apiUrl, HttpMethod.POST, req, Map.class);

            Map<?, ?> data = res.getBody();
            if (data == null) return fallbackStretchResult("AI 응답 없음", p);

            List<?> choices = (List<?>) data.get("choices");
            if (choices == null || choices.isEmpty()) return fallbackStretchResult("AI choices 비어있음", p);

            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) first.get("message");
            String content = message != null ? (String) message.get("content") : null;
            if (content == null || content.isBlank()) return fallbackStretchResult("AI content 비어있음", p);

            @SuppressWarnings("unchecked")
            Map<String, Object> json = om.readValue(content, Map.class);

            // 5) 데이터 파싱
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> painAreas =
                    (List<Map<String, Object>>) json.getOrDefault("painAreas", List.of());

            @SuppressWarnings("unchecked")
            List<String> globalCautions =
                    (List<String>) json.getOrDefault("globalCautions", List.of());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> intervals =
                    (List<Map<String, Object>>) json.getOrDefault("intervals", List.of());

            // 공원 추천 파싱 (GPS 없을 때를 위한 기본값)
            @SuppressWarnings("unchecked")
            Map<String, Object> recMap = (Map<String, Object>) json.get("recommendation");

            Map<String, Object> parkInfo = new LinkedHashMap<>();
            if (recMap != null) {
                String keyword = (String) recMap.getOrDefault("searchKeyword", "공원");
                String type = (String) recMap.getOrDefault("parkType", "근처 공원");
                String reason = (String) recMap.getOrDefault("reason", "가벼운 산책을 추천합니다.");

                String query = "내주변 " + keyword;

                parkInfo.put("추천장소", type);
                parkInfo.put("추천이유", reason);
                parkInfo.put("검색키워드", query);

                parkInfo.put("지도링크", "https://map.kakao.com/link/search/" + query);
            } else {
                parkInfo.put("추천장소", "가까운 공원");
                parkInfo.put("추천이유", "가벼운 산책으로 기분 전환을 해보세요.");
                parkInfo.put("검색키워드", "내주변 공원");

                parkInfo.put("지도링크", "https://map.kakao.com/link/search/내주변 공원");
            }

            // 6) 통증 부위별 영상 매핑
            for (Map<String, Object> area : painAreas) {
                String code = String.valueOf(area.getOrDefault("areaCode", ""));
                String name = String.valueOf(area.getOrDefault("koreanName", code));
                area.put("videos", presetVideosForArea(code, name));
            }

            String painName = null;
            List<Map<String, Object>> videos = List.of();

            if (!painAreas.isEmpty()) {
                painName = painAreas.stream()
                        .map(area -> String.valueOf(area.getOrDefault("koreanName", "")))
                        .filter(n -> !n.isBlank())
                        .collect(java.util.stream.Collectors.joining(", "));

                Map<String, Object> mainArea = painAreas.get(0);
                videos = (List<Map<String, Object>>) mainArea.getOrDefault("videos", List.of());
            }

            if (videos.isEmpty()) {
                videos = presetVideosForArea("default", "전신");
            }

            // 7) 최종 응답
            List<Map<String, Object>> koreanVideos = new ArrayList<>();
            for (Map<String, Object> v : videos) {
                Map<String, Object> kv = new LinkedHashMap<>();
                kv.put("제목", v.get("title"));
                kv.put("영상주소", v.get("videoUrl"));
                koreanVideos.add(kv);
            }

            List<Map<String, Object>> koreanIntervals = new ArrayList<>();
            for (Map<String, Object> in : intervals) {
                Map<String, Object> ki = new LinkedHashMap<>();
                ki.put("루틴명", in.get("name"));
                ki.put("세트수", in.get("sets"));
                ki.put("운동시간분", in.get("workMinutes"));
                ki.put("휴식시간분", in.get("restMinutes"));
                ki.put("강도", in.get("intensity"));
                ki.put("설명", in.get("description"));
                koreanIntervals.add(ki);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("사용자코드", p.userCode);
            result.put("통증부위", painName);
            result.put("스트레칭영상", koreanVideos);
            result.put("인터벌운동", koreanIntervals);
            result.put("공원추천", parkInfo);
            result.put("주의사항", globalCautions);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackStretchResult("예외 발생: " + e.getMessage(), p);
        }
    }

    // ===================== Preset & DB Methods =====================

    private List<Map<String, Object>> presetVideosForArea(String areaCode, String areaName) {
        List<Map<String, Object>> list = new ArrayList<>();
        switch (areaCode) {
            case "neck":
                list.add(video("목 통증 잡는 거북목 교정", "https://www.youtube.com/watch?v=3vvDOenCrCo"));
                list.add(video("어디서든 할 수 있는 목 스트레칭", "https://www.youtube.com/watch?v=PVwP8VaM9M4"));
                break;
            case "shoulder":
                list.add(video("시니어 어깨관절 스트레칭", "https://www.youtube.com/watch?v=P9YC2UWQi58"));
                list.add(video("굳은 어깨 펴는 전면부 운동", "https://www.youtube.com/watch?v=dZ97-TBoMN4"));
                break;
            case "upper_back":
                list.add(video("굽은 등 펴는 하루 10분 운동", "https://www.youtube.com/watch?v=6ISb-wS992Q"));
                list.add(video("목 어깨 등 한번에 펴는 운동", "https://www.youtube.com/watch?v=1Dhqq8OYsxY"));
                break;
            case "lower_back":
                list.add(video("5분 순삭 허리 통증 완화", "https://www.youtube.com/watch?v=CBC025SQ1iQ"));
                list.add(video("전문가가 추천하는 허리 스트레칭", "https://www.youtube.com/watch?v=8RUW6K3YYuA"));
                break;
            case "knee":
                list.add(video("무릎 통증 싹 잡는 튼튼 운동", "https://www.youtube.com/watch?v=LgkuHY9MtRs"));
                list.add(video("수술 없이 걷게 만드는 무릎 운동", "https://www.youtube.com/watch?v=DM57i_DuNLY"));
                break;
            case "wrist":
                list.add(video("손목 강화 헬스투데이 스트레칭", "https://www.youtube.com/watch?v=zKFhDgBqUGk"));
                list.add(video("손목 통증 줄이는 5초 비법", "https://www.youtube.com/watch?v=uUixf-O7XBA"));
                break;
            case "ankle":
                list.add(video("발목 강화와 통증 완화", "https://www.youtube.com/watch?v=f2q5_lvR4E8"));
                list.add(video("발목 튼튼해지는 4가지 동작", "https://www.youtube.com/watch?v=v0Z0r_VCcRI"));
                break;
            case "hip":
                list.add(video("시니어 고관절 유연성 기르기", "https://www.youtube.com/watch?v=xBQ7PYjBQ_A"));
                list.add(video("허리 엉덩이 허벅지 통증 잡는 운동", "https://www.youtube.com/watch?v=t4nZjcscZr8"));
                break;
            default:
                list.add(video("어르신도 쉬운 전신 스트레칭", "https://www.youtube.com/watch?v=W-UKAo6sjsU"));
                list.add(video("따라만 하면 시원한 아침 체조", "https://www.youtube.com/watch?v=tdeDK72dboM"));
                break;
        }
        return list;
    }

    private Map<String, Object> video(String title, String url) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("title", title);
        v.put("videoUrl", url);
        return v;
    }

    private Map<String, Object> fallbackStretchResult(String reason, UserProfile p) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("사용자코드", p.userCode);
        result.put("통증부위", null);
        result.put("스트레칭영상", List.of());
        result.put("인터벌운동", List.of());
        result.put("공원추천", null);
        result.put("실패이유", reason);
        return result;
    }

    private UserProfile findUserByCode(String userCode) {
        String sql = """
                SELECT id, user_code, name, gender, age, height, weight, chronic_diseases
                FROM users
                WHERE user_code = ?
                LIMIT 1
                """;
        return jdbc.query(sql, userMapper(), userCode)
                .stream().findFirst().orElseGet(UserProfile::empty);
    }

    private UserProfile findUserById(Long id) {
        String sql = """
                SELECT id, user_code, name, gender, age, height, weight, chronic_diseases
                FROM users
                WHERE id = ?
                LIMIT 1
                """;
        return jdbc.query(sql, userMapper(), id)
                .stream().findFirst().orElseGet(UserProfile::empty);
    }

    private RowMapper<UserProfile> userMapper() {
        return (ResultSet rs, int rowNum) -> {
            UserProfile p = new UserProfile();
            p.id = rs.getLong("id");
            p.userCode = rs.getString("user_code");
            p.name = rs.getString("name");
            p.gender = opt(rs.getString("gender"), "unknown");
            p.age = (Integer) rs.getObject("age");
            p.height = rs.getObject("height") == null ? null : ((Number) rs.getObject("height")).doubleValue();
            p.weight = rs.getObject("weight") == null ? null : ((Number) rs.getObject("weight")).doubleValue();
            p.chronicDiseases = csv(rs.getString("chronic_diseases"));
            p.bmi = calcBmi(p.height, p.weight);
            return p;
        };
    }

    private String resolveApiKey() {
        String env = System.getenv("OPENAI_API_KEY");
        if (env != null && !env.isBlank()) return env;
        if (apiKeyFromProps != null && !apiKeyFromProps.isBlank()) return apiKeyFromProps;
        throw new IllegalStateException("OpenAI API key missing");
    }

    private static String opt(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    private static List<String> csv(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("[,;\\s]+")).map(String::trim).filter(v -> !v.isBlank()).toList();
    }

    private static Double calcBmi(Double height, Double weight) {
        if (height == null || weight == null) return null;
        double h = height > 3 ? height / 100.0 : height;
        if (h <= 0) return null;
        return weight / (h * h);
    }

    private static List<ChatMessage> lastMessages(List<ChatMessage> all, int limit) {
        if (all == null || all.isEmpty()) return List.of();
        int from = Math.max(0, all.size() - limit);
        return all.subList(from, all.size());
    }

    private static class UserProfile {
        Long id;
        String userCode;
        String name;
        String gender;
        Integer age;
        Double height;
        Double weight;
        List<String> chronicDiseases = new ArrayList<>();
        Double bmi;
        static UserProfile empty() {
            UserProfile p = new UserProfile();
            p.gender = "unknown";
            p.chronicDiseases = new ArrayList<>();
            return p;
        }
    }
}