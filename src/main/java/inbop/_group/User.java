package inbop._group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 10)
    private String userCode;  // 사용자 코드 (4자리 영문+숫자)
    
    private String name;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    
    @Column(length = 100000)
    private String faceUrl;  // Base64 이미지 또는 URL
    
    @Column(length = 500)
    private String chronicDiseases;  // 지병 (쉼표로 구분)
    
    @Column(length = 20)
    private String guardianPhone;  // 보호자 연락처
    
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer sproutCount = 0;  // 보유 새싹 개수
    
    private String lastSproutEarnedDate;  // 마지막 새싹 획득 날짜 (YYYY-MM-DD 형식)
    
    @PrePersist
    protected void onCreate() {
        if (sproutCount == null) {
            sproutCount = 0;
        }
    }
}

