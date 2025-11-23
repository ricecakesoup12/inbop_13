package inbop._group.sensor_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;

import inbop._group.VitalRecord;
import inbop._group.VitalRecordRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class SerialReaderService {
    private static final Logger log = LoggerFactory.getLogger(SerialReaderService.class);

    private volatile boolean debugMode = false;
    private volatile int debugHr = 80;

    @Value("${serial.port:COM4}")
    private String portName;

    @Value("${bt.serial.port:COM4}")
    private String btPortName;

    @Value("${serial.baud:9600}")
    private int baudRate;

    @Value("${serial.userId:1}")  // 기본 사용자 ID (설정 파일에서 변경 가능)
    private Long defaultUserId;

    @Autowired
    private VitalRecordRepository vitalRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;


    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SerialPort port;
    private ExecutorService readerPool;
    private volatile boolean running = false;
    private BufferedReader reader;

    // 최근 센서 데이터 (JSON 형식)
    private final AtomicReference<Map<String, Object>> lastSensorData = new AtomicReference<>(new HashMap<>());

    // SSE 스트림 (JSON 문자열)
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public List<String> listPorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(p -> p.getSystemPortName() + " | " + p.getDescriptivePortName())
                .toList();
    }

    @PostConstruct
    public void init() {
        System.out.println("BTSerialReader init...");
        openPortOnce();     // 기존 블루투스 포트 오픈
    }
    /**
     * 연결 상태 외부에서 확인 가능
     */
    public boolean isConnected() {
        return running;
    }

    public reactor.core.publisher.Flux<String> flux() {
        return sink.asFlux();
    }

    /**
     * 블루투스 포트 1회 오픈 시도
     */

    private void openPortOnce() {
        System.out.println("SerialReaderService openPortOnce...");
        port = SerialPort.getCommPort(btPortName);
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        if (!port.openPort()) {
            System.out.println("[BT] Could not open serial port: " + btPortName);
            running = false;
            return; // 더 이상 시도하지 않음
        }

        System.out.println("[BT] Port opened: " + btPortName);
        reader = new BufferedReader(
                new InputStreamReader(port.getInputStream(), StandardCharsets.UTF_8));

        running = true;
        executor.submit(this::readLoop);
    }


    /* 유선
    @PostConstruct
    public void start() {
        log.info("Available serial ports: {}", String.join(", ", listPorts()));
        log.info("Trying to open serial: {} ({} bps)", portName, baud);

        // 1) 포트 객체/파라미터 설정
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baud);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        // 2) 오픈
        if (!port.openPort()) {
            log.warn("시리얼 포트를 열 수 없습니다: {}  (장치 관리자 포트/점유 여부 확인)", portName);
            System.out.println("시리얼 포트를 열 수 없습니다.");
            return;
        }

        log.info("Serial connected: {} ({} bps)", portName, baud);

        // 3) 리더 스레드 시작
        running = true;
        readerPool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "serial-reader");
            t.setDaemon(true);
            return t;
        });
        readerPool.submit(this::readLoop);
    }
    */

    private void readLoop() {
        final var in = port.getInputStream();
        final var sb = new StringBuilder();

        try {
            while (running) {
                int b;
                try {
                    b = in.read();
                } catch (com.fazecast.jSerialComm.SerialPortTimeoutException te) {
                    continue;
                }

                if (b < 0) continue;

                char ch = (char) b;
                if (ch == '\n') {
                    String line = sb.toString().strip();
                    sb.setLength(0);
                    if (!line.isEmpty()) {
                        log.info("[SERIAL] {}", line);
                        processSensorData(line);
                    }
                } else if (ch != '\r') {
                    sb.append(ch);
                }
            }
        } catch (IOException e) {
            log.warn("Serial read IO issue: {}", e.toString());
        }
    }

    private void processSensorData(String rawData) {
        try {
            // JSON 파싱
            Map<String, Object> sensorData = objectMapper.readValue(rawData, Map.class);

            // HR 추출
            Integer hr = extractValue(sensorData, "hr", "HR", "heartRate", "bpm");

            if (hr == null) {
                log.warn("센서 데이터에 HR이 없습니다: {}", rawData);
                return;
            }

            // 센서 데이터 객체 생성
            Map<String, Object> processedData = new HashMap<>();
            processedData.put("hr", hr);
            processedData.put("timestamp", System.currentTimeMillis());
            processedData.put("raw", rawData);

            // 최근 데이터 업데이트
            lastSensorData.set(processedData);

            // JSON 문자열로 변환하여 SSE 스트림에 전송
            String jsonString = objectMapper.writeValueAsString(processedData);
            sink.tryEmitNext(jsonString);

            // DB에 저장
            saveToDatabase(hr);

            log.info("✅ 센서 데이터 처리 완료: HR={}", hr);
        } catch (Exception e) {
            log.error("❌ 센서 데이터 파싱 실패: {}", rawData, e);
        }
    }

    private Integer extractValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else if (value instanceof String) {
                    try {
                        return Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

    private void saveToDatabase(Integer hr) {
        try {
            VitalRecord record = new VitalRecord();
            record.setUserId(defaultUserId);
            record.setHr(hr);
            record.setRecordedAt(LocalDateTime.now());

            vitalRecordRepository.save(record);
            log.debug("💾 DB 저장 완료: userId={}, hr={}", defaultUserId, hr);
        } catch (Exception e) {
            log.error("❌ DB 저장 실패", e);
        }
    }

    public Map<String, Object> getLastSensorData() {
        return lastSensorData.get();
    }

    public Flux<String> streamJson() {
        return sink.asFlux();
    }

    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();

        boolean portConnected = (port != null && port.isOpen());
        boolean connected = debugMode || portConnected;
        status.put("connected", connected);
        status.put("portName", portName);
        status.put("baud", baudRate);
        status.put("running", running);
        status.put("hasData", !lastSensorData.get().isEmpty());
        status.put("lastData", lastSensorData.get());
        status.put("availablePorts", listPorts());
        return status;
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (readerPool != null) readerPool.shutdownNow();
        if (port != null && port.isOpen()) {
            port.closePort();
            log.info("Serial disconnected: {}", portName);
        }
    }

    // 블루투스 재연결 시도
    public synchronized void reconnect() {
        System.out.println("[BT] reconnect() called");

        if (debugMode) {
            System.out.println("[BT] DEBUG MODE");
            running = false;
            startDebugLoop();
            return;
        }

        // 1) 기존 readLoop 중지 플래그
        running = false;

        // 2) 포트가 열려 있으면 닫기
        if (port != null && port.isOpen()) {
            try {
                System.out.println("[BT] Closing existing port: " + btPortName);
                port.closePort();
            } catch (Exception e) {
                System.out.println("[BT] Error while closing port: " + e.getMessage());
            }
        }

        // 3) 다시 열기
        openPortOnce();
    }



    // 블루투스 디버그 모드 시작 (프론트에서 hr 받아서 설정)
    public synchronized void startDebugMode(Integer hrFromRequest) {
        System.out.println("[DEBUG] startDebugMode called, hr = " + hrFromRequest);

        // 1) 실제 포트 읽기 중단
        running = false;

        if (port != null && port.isOpen()) {
            try {
                System.out.println("[DEBUG] Closing real port: " + btPortName);
                port.closePort();
            } catch (Exception e) {
                System.out.println("[DEBUG] Error while closing port: " + e.getMessage());
            }
        }

        // 2) 디버그 플래그 및 HR 설정
        debugMode = true;
        if (hrFromRequest != null && hrFromRequest > 0) {
            debugHr = hrFromRequest;
        } else {
            debugHr = 80; // 기본값
        }

        // 3) 디버그용 페이크 데이터 루프 시작
        startDebugLoop();
    }

    private String createFakeSensorJson(int fakeHr) throws Exception {
        Map<String, Object> data = new HashMap<>();

        data.put("ts", System.currentTimeMillis());     // millis() 대신 서버 시간
        data.put("raw", 500);                           // 임의 값
        data.put("sensor_avr", 505);                    // 임의 값
        data.put("bpm", fakeHr);                        // 프론트가 요청한 fake HR
        data.put("beat", "false");                   // true/false
        data.put("thr", 550);                           // 일반 threshold 값

        return objectMapper.writeValueAsString(data);
    }

    private void startDebugLoop() {
        // 디버그용 루프 시작
        running = true;

        executor.submit(() -> {
            System.out.println("[DEBUG] Fake sensor loop started. hr=" + debugHr);

            while (running && debugMode) {
                try {
                    int hr = debugHr; // 매 반복마다 최신 값 사용 (원하면 나중에 값 변경 가능)
                    String rawJson = createFakeSensorJson(debugHr);
                    Map<String, Object> sensorData =
                            objectMapper.readValue(rawJson, Map.class);

                    log.info("[DEBUG] Fake sensor data: {}", rawJson);

                    // 최근 데이터 업데이트
                    lastSensorData.set(sensorData);
                    sink.tryEmitNext(rawJson);

                    // DB 저장 (실제와 동일하게 처리)
                    saveToDatabase(hr);

                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("[DEBUG] Fake sensor loop error", e);
                }
            }

            System.out.println("[DEBUG] Fake sensor loop stopped.");
        });
    }
    public synchronized void stopDebugMode() {
        System.out.println("[DEBUG] stopDebugMode called");

        debugMode = false;
        running = false;

        // 포트를 닫을 필요는 없지만, 디버그 루프가 돌고 있었다면 자동 종료됨
        System.out.println("[DEBUG] 디버그 모드 종료.");
    }

}