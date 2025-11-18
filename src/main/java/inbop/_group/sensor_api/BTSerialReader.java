package inbop._group.sensor_api;

import com.fazecast.jSerialComm.SerialPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BTSerialReader {

    private final String portName;
    private final int baudRate;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private SerialPort port;
    private BufferedReader reader;

    // 구독자에게 라인 스트림을 브로드캐스트
    private final Sinks.Many<String> sink = Sinks.many().multicast().directBestEffort();
    @Getter
    private volatile String lastLine = null;

    public BTSerialReader(
            @Value("${BTserial.port}") String portName,
            @Value("${serial.baud}") int baudRate
    ) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    @PostConstruct
    public void init() {
        System.out.println("BTSerialReader init...");
        openPortOnce(); // 한 번만 시도
        /*
        while (!running) {
            openAndStart();
        }
         */
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
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        if (!port.openPort()) {
            System.out.println("[BT] Could not open serial port: " + portName);
            running = false;
            return; // 더 이상 시도하지 않음
        }

        System.out.println("[BT] Port opened: " + portName);
        reader = new BufferedReader(
                new InputStreamReader(port.getInputStream(), StandardCharsets.UTF_8));

        running = true;
        executor.submit(this::readLoop);
    }

    private void openAndStart() {
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

        if (!port.openPort()) {
            System.out.println("BT Could not open serial port: " + portName);
            return;
            //throw new IllegalStateException("BT 시리얼 포트를 열 수 없습니다: " + portName);
        }

        reader = new BufferedReader(
                new InputStreamReader(port.getInputStream(), StandardCharsets.UTF_8));

        running = true;
        executor.submit(this::readLoop);
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                lastLine = line;
                sink.tryEmitNext(line);
            }
        } catch (IOException e) {
            sink.tryEmitError(e);
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executor.shutdownNow();
        try { if (reader != null) reader.close(); } catch (Exception ignore) {}
        if (port != null && port.isOpen()) port.closePort();
    }
}
