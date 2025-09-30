package inbop._group.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiProxyService {

    private String schema = "{\"meta\": {\"version\": \"1.0.0\",\"generated_at\": \"20xx-xx-xxhxxm\",\"notes\": \"????\"},\"?_??_????_?\": 0,\"??\": {{ \"???\": \"??1\", \"??_?\": 0, \"??\": 0, \"??_??\": \"RPE(0-10)\" },{ \"???\": \"??2\", \"??_?\": 0, \"??\": 0, \"??_??\": \"RPE(0-10)\" },{ \"???\": \"??3\", \"??_?\": 0, \"??\": 0, \"??_??\": \"RPE(0-10)\" },{ \"???\": \"??4\", \"??_?\": 0, \"??\": 0, \"??_??\": \"RPE(0-10)\" }}\"??_??\": {\"RPE(0-10)\": \"0=??, 3=?? ???, 5=??, 7=?? ??, 9=?? ??\"}}";

    private final RestTemplate openAiRestTemplate;

    public AiProxyService(@Qualifier("openAiRestTemplate") RestTemplate openAiRestTemplate) {
        this.openAiRestTemplate = openAiRestTemplate;
    }

    public ResponseEntity<?> callOpenAiAndReturn(String message) {
        try {
            // 1) Body 구성 (Chat Completions 규격: model + messages 필수)
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "gpt-4o-mini");

            List<Map<String, Object>> messages = List.of(
                    Map.of("role", "system", "content", "You are a concise assistant. json 파일로 운동루틴 생성 출력 구조(반드시 일치해야함):" + schema),
                    Map.of("role", "user", "content", message)
            );
            body.put("messages", messages);

            // 2) JSON 직렬화 + 전송 직전 로그
            ObjectMapper om = new ObjectMapper();
            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            System.out.println("=== OpenAI Request JSON ===\n" + json);

            // 3) 헤더 (※ openAiRestTemplate에 Authorization 인터셉터가 없다면 여기서 넣어주세요)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // headers.setBearerAuth(System.getenv("OPENAI_API_KEY")); // 인터셉터 없다면 주석 해제

            // 4) 문자열 그대로 전송 (JSON이 확실히 전달됨)
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            ResponseEntity<Map> res = openAiRestTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            // 5) 응답 파싱
            String summary = "";
            Map<?, ?> resBody = res.getBody();
            if (resBody != null) {
                Object choices = resBody.get("choices");
                if (choices instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> m) {
                        Object msg = m.get("message");
                        if (msg instanceof Map<?, ?> mm) {
                            Object content = mm.get("content");
                            if (content != null) summary = content.toString();
                        }
                    }
                }
            }

            Map<String, Object> out = new HashMap<>();
            out.put("summary", summary);
            out.put("model", "gpt-4o-mini");
            System.out.println("=== OpenAI Raw Response ===");
            System.out.println(res.getBody());
            return ResponseEntity.status(res.getStatusCode()).body(out);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "OpenAI 호출 실패");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }
    }

}
