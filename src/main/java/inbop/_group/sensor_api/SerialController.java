package inbop._group.sensor_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/serial")
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
    @GetMapping("/latest")
    public Map<String, Object> latest() {
        return service.getLastSensorData();
    }

    // 실시간 스트림 (Server-Sent Events) - JSON 형식
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return service.streamJson();
    }

    // 센서 연결 상태 확인
    @GetMapping("/status")
    public Map<String, Object> status() {
        return service.getConnectionStatus();
    }

}

