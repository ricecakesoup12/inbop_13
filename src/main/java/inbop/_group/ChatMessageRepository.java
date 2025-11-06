package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    /**
     * 특정 사용자의 채팅 메시지 조회 (시간순 정렬)
     */
    List<ChatMessage> findByUserIdOrderByTimestampAsc(String userId);
    
    /**
     * 모든 채팅 메시지 조회 (시간순 정렬)
     */
    List<ChatMessage> findAllByOrderByTimestampAsc();
}

