package inbop._group.controller;


import inbop._group.service.AiProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    public TestController(AiProxyService aiProxyService) {
        this.aiProxyService = aiProxyService;
        System.out.println("controller 생성");
    }

    private final AiProxyService aiProxyService;

    @PostMapping("/api/survey")
    public ResponseEntity<String> printRaw(@RequestBody String rawJson) {
        System.out.println("=== Received RAW JSON ===");
        System.out.println(rawJson);
        aiProxyService.callOpenAiAndReturn(rawJson);
        return ResponseEntity.ok("ok");
    }

}
