package inbop._group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stretch")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:5174"
})
public class StretchController {

    private final StretchGenerator stretchGenerator;

    public StretchController(StretchGenerator stretchGenerator) {
        this.stretchGenerator = stretchGenerator;
    }

    // userCode 기준 스트레칭/인터벌 추천
    @GetMapping("/user-code/{userCode}")
    public ResponseEntity<Map<String, Object>> byUserCode(@PathVariable String userCode) {
        Map<String, Object> result = stretchGenerator.recommendStretchingByUserCode(userCode);
        return ResponseEntity.ok(result);
    }
}
