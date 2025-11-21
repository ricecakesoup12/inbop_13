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
        /*
        System.out.println("BTSerialReader init...");
        openPortOnce(); // 한 번만 시도
        */
        while (!running) {
            openPortOnce();
        }

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

            // HR과 SpO2 추출
            Integer hr = extractValue(sensorData, "hr", "HR", "heartRate", "bpm");
            Integer spo2 = extractValue(sensorData, "spo2", "SpO2", "spO2", "oxygen");

            if (hr == null || spo2 == null) {
                log.warn("센서 데이터에 HR 또는 SpO2가 없습니다: {}", rawData);
                return;
            }

            // 센서 데이터 객체 생성
            Map<String, Object> processedData = new HashMap<>();
            processedData.put("hr", hr);
            processedData.put("spo2", spo2);
            processedData.put("timestamp", System.currentTimeMillis());
            processedData.put("raw", rawData);

            // 최근 데이터 업데이트
            lastSensorData.set(processedData);

            // JSON 문자열로 변환하여 SSE 스트림에 전송
            String jsonString = objectMapper.writeValueAsString(processedData);
            sink.tryEmitNext(jsonString);

            // DB에 저장
            saveToDatabase(hr, spo2);

            log.info("✅ 센서 데이터 처리 완료: HR={}, SpO2={}", hr, spo2);
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

    private void saveToDatabase(Integer hr, Integer spo2) {
        try {
            VitalRecord record = new VitalRecord();
            record.setUserId(defaultUserId);
            record.setHr(hr);
            record.setSpo2(spo2);
            record.setRecordedAt(LocalDateTime.now());

            vitalRecordRepository.save(record);
            log.debug("💾 DB 저장 완료: userId={}, hr={}, spo2={}", defaultUserId, hr, spo2);
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
        status.put("connected", port != null && port.isOpen());
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
}