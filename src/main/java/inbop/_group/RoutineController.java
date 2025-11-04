package inbop._group;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final AiRoutineGenerator generator;

    public RoutineController(AiRoutineGenerator generator) {
        this.generator = generator;
    }

    @GetMapping("/sample/{userCode}")
    public List<RoutineResponse> sample(@PathVariable String userCode,
                                        @RequestParam(defaultValue = "3") int count) {
        return generator.generateFromDbViaAI(userCode, count);
    }

    // 강도 조절 (세트/횟수/분 비례 스케일)
    @PostMapping("/adjust")
    public RoutineResponse adjust(@RequestBody RoutineResponse base,
                                  @RequestParam(defaultValue = "0") int delta) {
        return generator.adjustIntensity(base, delta);
    }
}
