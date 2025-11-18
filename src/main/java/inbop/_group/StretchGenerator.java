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
    // 내부 테스트용
    public Map<String, Object> recommendStretchingByUserId(Long userId) {
        UserProfile p = findUserById(userId);
        return recommendStretchingInternal(p);
    }

    // ===================== 메인 로직 =====================

    private Map<String, Object> recommendStretchingInternal(UserProfile p) {
        System.out.println("=== AI 운동 추천 시작 ===");
        System.out.println("사용자 ID: " + p.id);
        System.out.println("사용자 코드: " + p.userCode);
        System.out.println("사용자 이름: " + p.name);
        
        // 1) 최근 채팅 불러오기 (chat_messages.user_id 에 숫자 ID가 저장됨)
        // 채팅 메시지는 숫자 ID로 저장되므로 ID를 우선 사용
        String userKey = p.id != null ? String.valueOf(p.id) : p.userCode;

        System.out.println("채팅 조회용 userKey: " + userKey + " (ID 우선 사용)");
        
        List<ChatMessage> allMessages = userKey != null
                ? chatMessageRepository.findByUserIdOrderByTimestampAsc(userKey)
                : List.of();

        System.out.println("조회된 전체 채팅 메시지 개수: " + allMessages.size());
        
        List<ChatMessage> recent = lastMessages(allMessages, 30); // 최근 30개만 사용
        
        System.out.println("최근 30개 메시지 개수: " + recent.size());

        String recentText = recent.stream()
                .filter(m -> "user".equalsIgnoreCase(m.getSender()))
                .map(ChatMessage::getMessage)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> "- " + s)
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "\n") + b);
        
        long userMessageCount = recent.stream()
                .filter(m -> "user".equalsIgnoreCase(m.getSender())).count();
        System.out.println("사용자가 보낸 메시지 개수: " + userMessageCount);
        System.out.println("AI에 전송할 사용자 메시지:");
        System.out.println(recentText.isBlank() ? "(메시지 없음)" : recentText);

        // 2) 유저 프로필 문자열 만들기
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

        // 3) system 프롬프트 읽기
        String system;
        try (InputStreamReader reader =
                     new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            system = FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new RuntimeException("System 프롬프트를 읽는 데 실패했습니다.", e);
        }

        // 4) OpenAI 요청 구성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.3);
        body.put("max_tokens", 600);
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

            // 5) painAreas, globalCautions, intervals 꺼내기
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> painAreas =
                    (List<Map<String, Object>>) json.getOrDefault("painAreas", List.of());

            @SuppressWarnings("unchecked")
            List<String> globalCautions =
                    (List<String>) json.getOrDefault("globalCautions", List.of());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> intervals =
                    (List<Map<String, Object>>) json.getOrDefault("intervals", List.of());
            
            System.out.println("=== OpenAI 응답 분석 ===");
            System.out.println("painAreas 개수: " + painAreas.size());
            if (!painAreas.isEmpty()) {
                System.out.println("painAreas 상세: " + painAreas);
            }
            System.out.println("intervals 개수: " + intervals.size());
            System.out.println("=====================");

            // 6) 각 통증 부위별로 "미리 정해둔" 유튜브 영상 목록 videos 필드로 추가
            for (Map<String, Object> area : painAreas) {
                String code = String.valueOf(area.getOrDefault("areaCode", ""));
                String name = String.valueOf(area.getOrDefault("koreanName", code));
                List<Map<String, Object>> preset = presetVideosForArea(code, name);
                area.put("videos", preset);
            }

            // 7) 모든 통증부위를 콤마로 연결하고, 첫 번째 부위의 영상 사용
            Map<String, Object> mainArea = painAreas.isEmpty() ? null : painAreas.get(0);

            String painName = null;
            List<Map<String, Object>> videos = List.of();

            if (!painAreas.isEmpty()) {
                // 모든 통증 부위의 이름을 콤마로 연결
                painName = painAreas.stream()
                        .map(area -> String.valueOf(
                                area.getOrDefault("koreanName",
                                        area.getOrDefault("areaCode", ""))))
                        .filter(name -> !name.isBlank())
                        .collect(java.util.stream.Collectors.joining(", "));
                
                // 첫 번째 부위의 영상 사용
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> v =
                        (List<Map<String, Object>>) mainArea.getOrDefault("videos", List.of());
                videos = v;
            }
            
            // 통증 부위가 없거나 영상이 비어있으면 기본 스트레칭 영상 제공
            if (videos.isEmpty()) {
                System.out.println("⚠️ 통증 부위가 없거나 영상이 비어있어 기본 스트레칭 영상을 제공합니다.");
                videos = presetVideosForArea("default", "전신");
            }

            // 스트레칭 영상 변환
            List<Map<String, Object>> koreanVideos = new ArrayList<>();
            System.out.println("📹 스트레칭 영상 개수: " + videos.size());
            for (Map<String, Object> v : videos) {
                Map<String, Object> kv = new LinkedHashMap<>();
                kv.put("제목", v.get("title"));
                kv.put("영상주소", v.get("videoUrl"));
                koreanVideos.add(kv);
                System.out.println("  - 제목: " + v.get("title") + ", URL: " + v.get("videoUrl"));
            }

            // 인터벌 운동 변환
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

            // 최종 응답
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("사용자코드", p.userCode);
            result.put("통증부위", painName);      // ex) "허리"
            result.put("스트레칭영상", koreanVideos);
            result.put("인터벌운동", koreanIntervals);
            result.put("주의사항", globalCautions);

            System.out.println("✅ 최종 응답 생성 완료:");
            System.out.println("  - 통증부위: " + painName);
            System.out.println("  - 스트레칭영상 개수: " + koreanVideos.size());
            System.out.println("  - 인터벌운동 개수: " + koreanIntervals.size());

            return result;

        } catch (Exception e) {
            return fallbackStretchResult("예외: " + e.getClass().getSimpleName(), p);
        }
    }

    // ===================== 부위별 고정 스트레칭 영상 =====================

    private List<Map<String, Object>> presetVideosForArea(String areaCode, String areaName) {
        List<Map<String, Object>> list = new ArrayList<>();

        switch (areaCode) {
            case "neck": // 목
                list.add(video(
                        "목 통증 잡는 앉아서 하는 거북목 교정 운동",
                        "https://www.youtube.com/watch?v=3vvDOenCrCo"
                ));
                list.add(video(
                        "잘못된 목 스트레칭 STOP! 어디서든 할 수 있는 목 스트레칭",
                        "https://www.youtube.com/watch?v=PVwP8VaM9M4"
                ));
                break;

            case "shoulder": // 어깨
                list.add(video(
                        "시니어 어깨관절 스트레칭",
                        "https://www.youtube.com/watch?v=P9YC2UWQi58"
                ));
                list.add(video(
                        "[시니어홈트] 어깨가 굳었다면? 전면부 근육을 늘려야 합니다",
                        "https://www.youtube.com/watch?v=dZ97-TBoMN4"
                ));
                break;

            case "upper_back": // 등
                list.add(video(
                        "나이 들어 틀어진 척추, 하루 10분 초간단 운동법",
                        "https://www.youtube.com/watch?v=6ISb-wS992Q"
                ));
                list.add(video(
                        "오래 앉아 있으면 무너지는 목, 어깨! 여기 스트레칭하면 쫙 펴집니다",
                        "https://www.youtube.com/watch?v=1Dhqq8OYsxY"
                ));
                break;

            case "lower_back": // 허리
                list.add(video(
                        "5분이면 끝! 허리 통증 풀어주는 허리 스트레칭 (시니어 홈트)",
                        "https://www.youtube.com/watch?v=CBC025SQ1iQ"
                ));
                list.add(video(
                        "전문가가 알려주는 간단하지만 최고 효과 허리 스트레칭",
                        "https://www.youtube.com/watch?v=8RUW6K3YYuA"
                ));
                break;

            case "knee": // 무릎
                list.add(video(
                        "무릎 아플 땐 통증 싹~ 무릎 튼튼 운동 (시니어 홈트)",
                        "https://www.youtube.com/watch?v=LgkuHY9MtRs"
                ));
                list.add(video(
                        "무릎통증 90% 끝냅니다, 수술 없이 걷게 만든 4가지 스트레칭",
                        "https://www.youtube.com/watch?v=DM57i_DuNLY"
                ));
                break;

            case "wrist": // 손목
                list.add(video(
                        "헬스투데이 - 손목 강화 손목 스트레칭",
                        "https://www.youtube.com/watch?v=zKFhDgBqUGk"
                ));
                list.add(video(
                        "손목 통증을 줄이는 5초 스트레칭",
                        "https://www.youtube.com/watch?v=uUixf-O7XBA"
                ));
                break;

            case "ankle": // 발목
                list.add(video(
                        "완치하기 쉽지 않은 발목! 간단한 운동으로 발목 강화하기",
                        "https://www.youtube.com/watch?v=f2q5_lvR4E8"
                ));
                list.add(video(
                        "발목통증 완화를 돕는 발목 강화운동 4가지",
                        "https://www.youtube.com/watch?v=v0Z0r_VCcRI"
                ));
                break;

            case "hip": // 엉덩이/고관절
                list.add(video(
                        "시니어 고관절 스트레칭 방법",
                        "https://www.youtube.com/watch?v=xBQ7PYjBQ_A"
                ));
                list.add(video(
                        "허리, 엉덩이, 허벅지 통증까지 한 번에 잡아주는 고관절 운동",
                        "https://www.youtube.com/watch?v=t4nZjcscZr8"
                ));
                break;

            default: // 그 외에는 전신/기본 스트레칭
                list.add(video(
                        "어르신도 쉽게 할 수 있는 시니어 스트레칭",
                        "https://www.youtube.com/watch?v=W-UKAo6sjsU"
                ));
                list.add(video(
                        "스트레칭, 따라만 하면 시원해집니다!",
                        "https://www.youtube.com/watch?v=tdeDK72dboM"
                ));
                break;
        }

        return list;
    }

    // 공통 영상 Map 생성 유틸
    private Map<String, Object> video(String title, String url) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("title", title);
        v.put("videoUrl", url);
        return v;
    }

    // ===================== fallback =====================

    private Map<String, Object> fallbackStretchResult(String reason, UserProfile p) {
        Map<String, Object> result = new LinkedHashMap<>();
        // ID는 노출 안 함
        result.put("사용자코드", p.userCode);
        result.put("통증부위", null);
        result.put("스트레칭영상", List.of());
        result.put("인터벌운동", List.of());
        result.put("실패이유", reason);
        result.put("출처", "fallback");
        return result;
    }

    // ===================== DB 조회 =====================

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

    // ===================== 유틸 =====================

    private String resolveApiKey() {
        String env = System.getenv("OPENAI_API_KEY");
        if (env != null && !env.isBlank()) return env;
        if (apiKeyFromProps != null && !apiKeyFromProps.isBlank()) return apiKeyFromProps;
        throw new IllegalStateException("OpenAI API key not found. Set OPENAI_API_KEY or openai.api.key.");
    }

    private static String opt(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private static List<String> csv(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("[,;\\s]+"))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .toList();
    }

    private static Double calcBmi(Double height, Double weight) {
        if (height == null || weight == null) return null;
        double h = height > 3 ? height / 100.0 : height; // cm일 경우 m로 변환
        if (h <= 0) return null;
        return weight / (h * h);
    }

    private static List<ChatMessage> lastMessages(List<ChatMessage> all, int limit) {
        if (all == null || all.isEmpty()) return List.of();
        int from = Math.max(0, all.size() - limit);
        return all.subList(from, all.size());
    }

    // ===================== 내부 모델 =====================

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
