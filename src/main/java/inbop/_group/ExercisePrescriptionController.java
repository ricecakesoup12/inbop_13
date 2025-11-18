package inbop._group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exercise-prescriptions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", 
                        "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"})
public class ExercisePrescriptionController {

    private final ExercisePrescriptionRepository prescriptionRepository;

    public ExercisePrescriptionController(ExercisePrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * 특정 사용자의 처방 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExercisePrescription>> getPrescriptionsByUser(@PathVariable String userId) {
        System.out.println("=== 사용자 처방 조회 ===");
        System.out.println("userId: " + userId);
        
        List<ExercisePrescription> prescriptions = prescriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("✅ 처방 개수: " + prescriptions.size());
        
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * 특정 사용자의 대기 중인 처방 조회
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<ExercisePrescription> getPendingPrescription(@PathVariable String userId) {
        System.out.println("=== 대기 중인 처방 조회 ===");
        System.out.println("userId: " + userId);
        
        Optional<ExercisePrescription> pending = prescriptionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, "PENDING");
        
        if (pending.isPresent()) {
            System.out.println("✅ 대기 중인 처방 발견: ID=" + pending.get().getId());
            return ResponseEntity.ok(pending.get());
        } else {
            System.out.println("⚠️ 대기 중인 처방 없음");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 모든 처방 조회 (보호자용)
     */
    @GetMapping
    public ResponseEntity<List<ExercisePrescription>> getAllPrescriptions() {
        List<ExercisePrescription> prescriptions = prescriptionRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * 처방 전송
     */
    @PostMapping
    public ResponseEntity<ExercisePrescription> createPrescription(@RequestBody ExercisePrescription prescription) {
        System.out.println("=== 처방 전송 요청 받음 ===");
        System.out.println("userId: " + prescription.getUserId());
        System.out.println("startStretchingMinutes: " + prescription.getStartStretchingMinutes());
        System.out.println("startStretchingUrl: " + prescription.getStartStretchingUrl());
        System.out.println("walkingMinutes: " + prescription.getWalkingMinutes());
        System.out.println("runningMinutes: " + prescription.getRunningMinutes());
        System.out.println("sets: " + prescription.getSets());
        System.out.println("endStretchingMinutes: " + prescription.getEndStretchingMinutes());
        System.out.println("endStretchingUrl: " + prescription.getEndStretchingUrl());
        
        try {
            prescription.setStatus("PENDING");
            ExercisePrescription saved = prescriptionRepository.save(prescription);
            System.out.println("✅ 처방 저장 완료: ID=" + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("❌ 처방 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 처방 수락
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<ExercisePrescription> acceptPrescription(@PathVariable Long id) {
        System.out.println("=== 처방 수락 ===");
        System.out.println("prescriptionId: " + id);
        
        ExercisePrescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("처방을 찾을 수 없습니다."));
        
        prescription.setStatus("ACCEPTED");
        prescription.setAcceptedAt(java.time.LocalDateTime.now());
        ExercisePrescription updated = prescriptionRepository.save(prescription);
        
        System.out.println("✅ 처방 수락 완료: ID=" + updated.getId() + ", status=" + updated.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * 처방 거부
     */
    @PutMapping("/{id}/decline")
    public ResponseEntity<ExercisePrescription> declinePrescription(@PathVariable Long id) {
        System.out.println("=== 처방 거부 ===");
        System.out.println("prescriptionId: " + id);
        
        ExercisePrescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("처방을 찾을 수 없습니다."));
        
        prescription.setStatus("DECLINED");
        ExercisePrescription updated = prescriptionRepository.save(prescription);
        
        System.out.println("✅ 처방 거부 완료: ID=" + updated.getId() + ", status=" + updated.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * 처방 완료
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ExercisePrescription> completePrescription(@PathVariable Long id) {
        System.out.println("=== 처방 완료 ===");
        System.out.println("prescriptionId: " + id);
        
        ExercisePrescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("처방을 찾을 수 없습니다."));
        
        prescription.setStatus("COMPLETED");
        prescription.setCompletedAt(java.time.LocalDateTime.now());
        ExercisePrescription updated = prescriptionRepository.save(prescription);
        
        System.out.println("✅ 처방 완료 저장: ID=" + updated.getId() + ", status=" + updated.getStatus() + ", completedAt=" + updated.getCompletedAt());
        return ResponseEntity.ok(updated);
    }
}

