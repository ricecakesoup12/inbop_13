package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/stretch")
@CrossOrigin(origins = {
        "http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
        "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"
})
public class StretchController {

    private final StretchGenerator stretchGenerator;
    private final KakaoParkService kakaoParkService;

    @Autowired
    public StretchController(StretchGenerator stretchGenerator, KakaoParkService kakaoParkService) {
        this.stretchGenerator = stretchGenerator;
        this.kakaoParkService = kakaoParkService;
    }

    @GetMapping("/user-code/{userCode}")
    public ResponseEntity<Map<String, Object>> getTotalRecommendation(
            @PathVariable String userCode,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon
    ) {
        // 1. AI 운동 처방 가져오기
        Map<String, Object> result = stretchGenerator.recommendStretchingByUserCode(userCode);

        // 2. 위치 정보가 있으면 카카오 공원 리스트로 '공원추천' 필드를 덮어쓰기
        if (lat != null && lon != null) {
            List<Map<String, Object>> realParks = kakaoParkService.getNearbyParks(lat, lon);

            // [핵심 수정] AI가 준 복잡한 정보 싹 지우고, 리스트만 딱 넣음
            result.put("공원추천", realParks);
        } else {
            // 위치 정보가 없으면 빈 리스트로 처리 (깔끔하게)
            result.put("공원추천", Collections.emptyList());
        }

        return ResponseEntity.ok(result);
    }
}