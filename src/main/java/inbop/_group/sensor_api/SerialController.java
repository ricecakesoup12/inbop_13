package inbop._group.sensor_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
    "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"})
public class SerialController {

    private final SerialReaderService service;
    private final ObjectMapper objectMapper;

    @Autowired
    public SerialController(SerialReaderService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    // 최근 1건
    @GetMapping("/serial/latest")
    public Map<String, Object> latest() {
        System.out.println("🔵 /api/latest 요청");
        return service.getLastSensorData();
    }

    // 실시간 스트림 (Server-Sent Events) - JSON 형식
    @GetMapping(value = "/serial/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        System.out.println("🔵 /api/stream SSE 구독");
        // SSE 형식: "data: {json}\n\n"
        return service.streamJson()
            .map(json -> "data: " + json + "\n\n");
    }

    // 센서 연결 상태 확인
    @GetMapping("/serial/status")
    public Map<String, Object> status() {
        System.out.println("🔵 /api/status 요청");
        return service.getConnectionStatus();
    }

    // 블루투스 재연결 시도
    @PostMapping("/bluetooth/reconnect")
    public ResponseEntity<?> reconnect(@RequestBody ReconnectRequest request) {
        System.out.println("재연결 요청 유저 ID: " + request.getUserId());
        service.reconnect();
        return ResponseEntity.ok("Reconnecting...");
    }

    @GetMapping("/serial/debug")
    public ResponseEntity<?> startDebug(
            @RequestParam Long userId,
            @RequestParam Integer hr
    ) {
        service.startDebugMode(hr);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "hr", hr
        ));
    }

    @GetMapping("/serial/debug/stop")
    public ResponseEntity<?> stopDebug() {

        service.stopDebugMode();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "디버그 모드가 종료되었습니다."
        ));
    }

}
