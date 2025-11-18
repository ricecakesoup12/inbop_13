package inbop._group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stretch")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:5174",
        "http://127.0.0.1:5175"
})
public class StretchController {

    private final StretchGenerator stretchGenerator;

    public StretchController(StretchGenerator stretchGenerator) {
        this.stretchGenerator = stretchGenerator;
    }

    // userCode 기준 스트레칭/인터벌 추천
    @GetMapping("/user-code/{userCode}")
    public ResponseEntity<Map<String, Object>> byUserCode(@PathVariable String userCode) {
        System.out.println("=== AI 운동 추천 API 호출 ===");
        System.out.println("userCode: " + userCode);
        Map<String, Object> result = stretchGenerator.recommendStretchingByUserCode(userCode);
        System.out.println("=== AI 운동 추천 API 응답 완료 ===");
        System.out.println("통증부위: " + result.get("통증부위"));
        System.out.println("스트레칭영상 개수: " + (result.get("스트레칭영상") != null ? ((java.util.List<?>) result.get("스트레칭영상")).size() : 0));
        System.out.println("인터벌운동 개수: " + (result.get("인터벌운동") != null ? ((java.util.List<?>) result.get("인터벌운동")).size() : 0));
        return ResponseEntity.ok(result);
    }
}
