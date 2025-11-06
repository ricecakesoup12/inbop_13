package inbop._group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
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
import java.util.stream.Collectors;

/**
  DB(users) → OpenAI 프롬프트 → AI가 루틴 생성
  -2~3개 플랜, 각 4~5개 운동
 */
@Service
@Primary
public class AiRoutineGenerator implements RoutineGenerator {

    // Open AI 설정
    @Value("${openai.api.key:#{null}}")
    private String apiKeyFromProps;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.timeout-ms:20000}")
    private int timeoutMs;

    private final ObjectMapper om = new ObjectMapper();

    // DB
    private final JdbcTemplate jdbc;

    @Autowired
    public AiRoutineGenerator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // AI Prompt
    @Value("classpath:ai-prompt.txt")
    private Resource systemPromptResource;

    // 메서드

    /** user_code 기준으로 2~3개 AI 루틴 생성 */
    public List<RoutineResponse> generateFromDbViaAI(String userCode, int count) {
        UserProfile p = findUserByCode(userCode);
        return aiPlansForUser(p, count);
    }

    /** id 기준으로 2~3개 AI 루틴 생성 */
    public List<RoutineResponse> generateFromDbViaAI(Long userId, int count) {
        UserProfile p = findUserById(userId);
        return aiPlansForUser(p, count);
    }

    /** 강도 조절: 세트/횟수/분 비례 스케일 (+20, -15 등) — record 구조용 */
    public RoutineResponse adjustIntensity(RoutineResponse base, int percentDelta) {
        List<RoutineItem> scaled = base.plan().stream()
                .map(it -> {
                    // exercises[] 각 라인 스케일 (예: "3세트 x 8회", "12분", "~6분")
                    String[] newExercises = scaleDetails(it.exercises(), percentDelta);
                    // duration도 스케일
                    String newDuration = scaleDuration(it.duration(), percentDelta);
                    return new RoutineItem(
                            it.day(),
                            it.title(),
                            it.description(),
                            newExercises,
                            newDuration
                    );
                })
                .toList();

        String summary = base.summary() +
                (percentDelta >= 0 ? " (강도 +" + percentDelta + "%)" : " (강도 " + percentDelta + "%)");

        return new RoutineResponse(summary, scaled);
    }

    @Override
    public Map<String, Object> generate(SurveyCreateRequestDto survey) {
        String userCode = survey != null && survey.getName() != null ? survey.getName() : "DEFAULT";
        List<RoutineResponse> plans = generateFromDbViaAI(userCode, 3);
        RoutineResponse r = plans.get(0);
        Map<String,Object> m = new LinkedHashMap<>();

        m.put("요약", r.summary());
        m.put("플랜", r.plan());
        m.put("총운동시간(분)", estimateTotalMinutes(r));
        m.put("경고", List.of("생성자: db+ai"));

        return m;
    }

    public RoutineResponse generate(RoutineRequest req) {
        String userCode = opt(req.goal(), "DEFAULT");
        List<RoutineResponse> list = generateFromDbViaAI(userCode, Math.max(2, Math.min(3, req.daysPerWeek())));
        return list.get(0);
    }

    // DB 조회

    private UserProfile findUserByCode(String userCode) {
        String sql = """
            SELECT id, user_code, name, gender, age, height, weight, face_url, chronic_diseases
            FROM users
            WHERE user_code=?
            LIMIT 1
        """;
        return jdbc.query(sql, userMapper(), userCode)
                .stream().findFirst().orElseGet(UserProfile::empty);
    }

    private UserProfile findUserById(Long id) {
        String sql = """
            SELECT id, user_code, name, gender, age, height, weight, face_url, chronic_diseases
            FROM users
            WHERE id=?
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
            p.height = rs.getObject("height") == null ? null : ((Number) rs.getObject("height")).doubleValue(); // cm 가정
            p.weight = rs.getObject("weight") == null ? null : ((Number) rs.getObject("weight")).doubleValue(); // kg
            p.faceUrl = rs.getString("face_url");
            p.chronicDiseases = csv(rs.getString("chronic_diseases"));
            p.bmi = calcBmi(p.height, p.weight);
            return p;
        };
    }

    // AI 호출

    private List<RoutineResponse> aiPlansForUser(UserProfile p, int count) {
        int nPlans = Math.max(2, Math.min(3, count));

        String profile = """
            name: %s
            age: %s
            gender: %s
            bmi: %s
            chronic_diseases: %s
            """.formatted(
                opt(p.name, "사용자"),
                p.age == null ? "unknown" : p.age,
                opt(p.gender, "unknown"),
                p.bmi == null ? "unknown" : String.format(Locale.US, "%.1f", p.bmi),
                p.chronicDiseases.isEmpty() ? "none" : String.join(", ", p.chronicDiseases)
        );

        String system;
        try (InputStreamReader reader = new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8)) { // <-- getInputStream()
            system = FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new RuntimeException("System 프롬프트를 읽는 데 실패했습니다.", e);
        }
        String userMsg = "USER PROFILE:\n" + profile;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.3);
        body.put("max_tokens", 900);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", userMsg)
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
            if (data == null) return List.of(fallbackRoutine("AI 응답 없음"));

            List<?> choices = (List<?>) data.get("choices");
            if (choices == null || choices.isEmpty()) return List.of(fallbackRoutine("AI choices 비어있음"));

            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) first.get("message");
            String content = message != null ? (String) message.get("content") : null;
            if (content == null || content.isBlank()) return List.of(fallbackRoutine("AI content 비어있음"));

            @SuppressWarnings("unchecked")
            Map<String, Object> json = om.readValue(content, Map.class);

            // --- JSON → RoutineResponse 리스트 변환 ---
            List<Map<String, Object>> plans = new ArrayList<>();

            // 1. "plans" 키(목록)가 있는지 확인
            Object plansObj = json.get("plans");
            if (plansObj instanceof List) {
                plans.addAll(safeListMap(plansObj));
            }
            // 2. "plans"가 없다면, 단일 플랜 형식({ "title": ... })인지 확인
            else if (json.containsKey("title") && json.containsKey("items")) {
                plans.add(json); // 단일 플랜 맵 자체를 리스트에 추가
            }
            if (plans.isEmpty()) return List.of(fallbackRoutine("AI plans 비어있음"));

            List<RoutineResponse> out = new ArrayList<>();
            for (Map<String, Object> plan : plans) {
                out.add(toRoutineResponseFromAI(plan));
                if (out.size() >= nPlans) break;
            }
            return out;

        } catch (Exception e) {
            return List.of(fallbackRoutine("예외: " + e.getClass().getSimpleName()));
        }
    }

    // ===================== 변환/가드 =====================

    private RoutineResponse toRoutineResponseFromAI(Map<String, Object> plan) {
        String title = String.valueOf(plan.getOrDefault("title", "시니어 맞춤 루틴"));
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> items = (List<Map<String,Object>>) plan.getOrDefault("items", List.of());

        List<RoutineItem> list = new ArrayList<>();
        int idx = 1; // 1일차, 2일차...
        int totalMin = 0;

        for (Map<String,Object> it : items) {
            // AI JSON에서 "하루치" 데이터를 읽어옵니다.
            String dayTitle = String.valueOf(it.getOrDefault("dayTitle", idx + "일차 운동"));
            String dayDescription = String.valueOf(it.getOrDefault("dayDescription", "안전하게 운동하세요."));
            Integer minutes = asInt(it.get("durationMinutes"));

            // AI JSON의 "exercises" (String 리스트)를 Java의 String[]로 변환
            @SuppressWarnings("unchecked")
            List<String> aiExercises = (List<String>) it.get("exercises");
            String[] exercisesArray;

            if (aiExercises != null && !aiExercises.isEmpty()) {
                exercisesArray = aiExercises.toArray(new String[0]);
            } else {
                // AI가 exercises를 안 줬을 때의 비상용
                exercisesArray = new String[]{ dayTitle, "총 " + (minutes != null ? minutes : 20) + "분", dayDescription };
            }

            String displayTime;
            if (minutes != null) {
                displayTime = minutes + "분";
                totalMin += minutes;
            } else {
                displayTime = "~20분"; // 비상용
                totalMin += 20;
            }

            // RoutineItem 생성자에 맞게 "하루치" 데이터를 삽입합니다.
            // new RoutineItem(int day, String title, String description, String[] exercises, String duration)
            list.add(new RoutineItem(idx++, dayTitle, dayDescription, exercisesArray, displayTime));
        }

        // (이하 로직은 동일)
        int declared = asInt(plan.get("planMinutesTotal")) != null ? asInt(plan.get("planMinutesTotal")) : totalMin;
        int finalTotal = declared > 0 ? declared : totalMin;

        String summary = "%s (총 약 %d분)".formatted(title, Math.max(20, finalTotal));
        return new RoutineResponse(summary, list);
    }

    private RoutineResponse fallbackRoutine(String reason) {
        List<RoutineItem> plan = List.of(
                new RoutineItem(1, "걷기(저강도)", "대화 가능한 호흡 유지",
                        new String[]{"걷기(저강도)", "12분", "대화 가능한 호흡 유지"}, "12분"),
                new RoutineItem(2, "의자 스쿼트", "무릎-발끝 정렬",
                        new String[]{"의자 스쿼트", "3세트 x 8회", "무릎-발끝 정렬"}, "~6분"),
                new RoutineItem(3, "세라밴드 로우", "어깨 내리고 날개뼈 모으기",
                        new String[]{"세라밴드 로우", "3세트 x 12회", "어깨 내리고 날개뼈 모으기"}, "~6분"),
                new RoutineItem(4, "의자 요가/호흡", "호흡 참지 않기",
                        new String[]{"의자 요가/호흡", "5분", "호흡 참지 않긔"}, "5분")
        );
        String summary = "안전·입문 루틴 (fallback: " + reason + ")";
        return new RoutineResponse(summary, plan);
    }

    // ===================== 공통 유틸 =====================

    private String resolveApiKey() {
        String env = System.getenv("OPENAI_API_KEY");
        if (env != null && !env.isBlank()) return env;
        if (apiKeyFromProps != null && !apiKeyFromProps.isBlank()) return apiKeyFromProps;
        throw new IllegalStateException("OpenAI API key not found. Set OPENAI_API_KEY or openai.api.key.");
    }

    private static String opt(String s, String def){
        return (s == null || s.isBlank()) ? def : s;
    }
    private static List<String> csv(String s){
        if(s==null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("[,;\\s]+"))
                .map(String::trim).filter(v->!v.isBlank()).toList();
    }
    private static Double calcBmi(Double height, Double weight){
        if(height==null || weight==null) return null;
        double h = height > 3 ? height/100.0 : height; // cm면 m로 변환
        if(h<=0) return null;
        return weight/(h*h);
    }
    private static int estimateTotalMinutes(RoutineResponse r){
        return r.plan().stream().mapToInt(it -> parseMinutes(it.duration())).sum();
    }
    private static int parseMinutes(String s){
        if(s==null) return 0;
        String t = s.replaceAll("[^0-9]", "");
        if(t.isBlank()) return 0;
        try { return Integer.parseInt(t); } catch (Exception e){ return 0; }
    }
    private static Integer asInt(Object o){
        if(o == null) return null;
        if(o instanceof Integer i) return i;
        if(o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e){ return null; }
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String,Object>> safeListMap(Object o){
        if(o instanceof List<?> l){
            try { return (List<Map<String,Object>>) (List<?>) l; } catch (Exception ignore) {}
        }
        return List.of();
    }

    /** exercises[] 각 라인을 비례 스케일링 */
    private static String[] scaleDetails(String[] exercises, int percent){
        if(exercises == null || exercises.length == 0) return exercises;
        String[] out = new String[exercises.length];
        for(int i=0;i<exercises.length;i++){
            out[i] = scaleExerciseLine(exercises[i], percent);
        }
        return out;
    }

    /** "3세트 x 8회", "12분", "~6분" 같은 표기 스케일 */
    private static String scaleExerciseLine(String line, int percent){
        if(line == null || line.isBlank()) return line;

        // 세트/횟수 패턴
        if(line.contains("세트") && line.contains("회")){
            try{
                String[] parts = line.split("세트\\s*x\\s*");
                int sets = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                int reps = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                sets = Math.max(1, Math.round(sets*(100+percent)/100f));
                reps = Math.max(4, Math.round(reps*(100+percent)/100f));
                return sets + "세트 x " + reps + "회";
            }catch(Exception ignore){}
        }

        // 분 표기 스케일
        if(line.contains("분")){
            return scaleDuration(line, percent);
        }

        return line;
    }

    /** "12분", "~6분" 스케일 */
    private static String scaleDuration(String s, int percent){
        if(s==null || s.isBlank()) return s;
        try{
            boolean approx = s.trim().startsWith("~");
            int min = Integer.parseInt(s.replaceAll("[^0-9]",""));
            min = Math.max(4, Math.round(min*(100+percent)/100f));
            return (approx ? "~" : "") + min + "분";
        }catch(Exception e){
            return s;
        }
    }

    // ===================== 내부 모델 =====================

    private static class UserProfile {
        Long id;
        String userCode;
        String name;
        String gender;
        Integer age;
        Double height; // cm 가정
        Double weight; // kg
        String faceUrl;
        List<String> chronicDiseases = new ArrayList<>();
        Double bmi;

        static UserProfile empty(){
            UserProfile p = new UserProfile();
            p.gender = "unknown";
            p.chronicDiseases = new ArrayList<>();
            return p;
        }
    }
}
