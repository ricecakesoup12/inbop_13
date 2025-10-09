package inbop._group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    // 사용자 등록
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
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
                .map(ResponseEntity::ok)
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

