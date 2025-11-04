package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    // 사용자 등록
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // 필수 필드 검증
            if (user.getUserCode() == null || user.getUserCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("사용자 코드는 필수입니다.");
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("이름은 필수입니다.");
            }
            if (user.getGender() == null || user.getGender().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("성별은 필수입니다.");
            }
            if (user.getAge() == null) {
                return ResponseEntity.badRequest().body("나이는 필수입니다.");
            }
            if (user.getHeight() == null) {
                return ResponseEntity.badRequest().body("키는 필수입니다.");
            }
            
            // faceUrl 길이 제한 체크 (100000자)
            if (user.getFaceUrl() != null && user.getFaceUrl().length() > 100000) {
                return ResponseEntity.badRequest().body("이미지 파일이 너무 큽니다. 5MB 이하의 이미지를 사용해주세요.");
            }
            
            // 디버깅: 받은 전체 데이터 확인
            System.out.println("=== 사용자 등록 - Request Body 확인 ===");
            System.out.println("userCode: " + user.getUserCode());
            System.out.println("name: " + user.getName());
            System.out.println("gender: " + user.getGender());
            System.out.println("age: " + user.getAge());
            System.out.println("height: " + user.getHeight());
            System.out.println("weight: " + user.getWeight());
            System.out.println("guardianPhone: " + user.getGuardianPhone());
            System.out.println("guardianPhone == null? " + (user.getGuardianPhone() == null));
            System.out.println("guardianPhone isEmpty? " + (user.getGuardianPhone() != null && user.getGuardianPhone().isEmpty()));
            
            // JSON 전체 출력
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(user);
                System.out.println("전체 JSON: " + json);
            } catch (Exception e) {
                System.out.println("JSON 변환 실패: " + e.getMessage());
            }
            
            User savedUser = userRepository.save(user);
            
            // 저장 후 확인
            System.out.println("=== 저장 후 확인 ===");
            System.out.println("저장된 보호자 연락처: " + savedUser.getGuardianPhone());
            System.out.println("저장된 ID: " + savedUser.getId());
            
            // DB에서 다시 조회해서 확인
            User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);
            if (retrievedUser != null) {
                System.out.println("DB 조회한 보호자 연락처: " + retrievedUser.getGuardianPhone());
            }
            
            return ResponseEntity.ok(savedUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("이미 사용 중인 사용자 코드입니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 사용자 목록 조회
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    // 사용자 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // 디버깅: 사용자 조회 시 보호자 연락처 확인
                    System.out.println("=== 사용자 조회 (ID: " + id + ") ===");
                    System.out.println("보호자 연락처: " + user.getGuardianPhone());
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 사용자 수정
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getName() != null) user.setName(userDetails.getName());
                    if (userDetails.getGender() != null) user.setGender(userDetails.getGender());
                    if (userDetails.getAge() != null) user.setAge(userDetails.getAge());
                    if (userDetails.getHeight() != null) user.setHeight(userDetails.getHeight());
                    if (userDetails.getWeight() != null) user.setWeight(userDetails.getWeight());
                    if (userDetails.getFaceUrl() != null) user.setFaceUrl(userDetails.getFaceUrl());
                    if (userDetails.getChronicDiseases() != null) user.setChronicDiseases(userDetails.getChronicDiseases());
                    if (userDetails.getGuardianPhone() != null) user.setGuardianPhone(userDetails.getGuardianPhone());
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // 코드로 사용자 찾기 (로그인)
    @GetMapping("/code/{userCode}")
    public ResponseEntity<User> getUserByCode(@PathVariable String userCode) {
        return userRepository.findByUserCode(userCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 코드 중복 확인
    @GetMapping("/code-check/{userCode}")
    public ResponseEntity<Boolean> checkUserCode(@PathVariable String userCode) {
        boolean exists = userRepository.existsByUserCode(userCode);
        return ResponseEntity.ok(exists);
    }
}

