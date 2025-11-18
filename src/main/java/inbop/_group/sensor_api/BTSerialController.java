package inbop._group.sensor_api;

import com.inboproject.projectstudy.Service.BTSerialReader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/api/bluetooth")
public class BTSerialController {

    private final BTSerialReader serialReader;

    public BTSerialController(BTSerialReader serialReader) {
        this.serialReader = serialReader;
    }

    /**
     * 🔹 최신 값 한 번만 가져오기 (ex. 현재 센서 값만 표시)
     * GET /api/bluetooth/latest
     */

    /**
     * 🔹 실시간 스트리밍 (SSE 방식)
     * 프론트에서 EventSource("/api/bluetooth/stream")로 연결
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamBluetoothData() {
        // 15초마다 heartbeat (연결 유지용)
        Flux<String> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(t -> ":heartbeat\n\n");

        // 실시간 블루투스 라인
        Flux<String> dataStream = serialReader.flux()
                .map(line -> "data: " + line + "\n\n");

        return Flux.merge(dataStream, heartbeat);
    }
}
