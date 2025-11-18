package inbop._group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-messages")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", 
                        "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"})
public class ChatMessageController {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 특정 사용자의 채팅 메시지 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatMessagesByUser(@PathVariable String userId) {
        System.out.println("=== 채팅 메시지 조회 ===");
        System.out.println("userId: " + userId);
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
        System.out.println("조회된 메시지 개수: " + messages.size());
        return ResponseEntity.ok(messages);
    }

    /**
     * 모든 채팅 메시지 조회 (보호자용)
     */
    @GetMapping
    public ResponseEntity<List<ChatMessage>> getAllChatMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAllByOrderByTimestampAsc();
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅 메시지 전송
     */
    @PostMapping
    public ResponseEntity<ChatMessage> sendChatMessage(@RequestBody ChatMessage chatMessage) {
        System.out.println("=== 채팅 메시지 저장 요청 ===");
        System.out.println("userId: " + chatMessage.getUserId());
        System.out.println("sender: " + chatMessage.getSender());
        System.out.println("senderName: " + chatMessage.getSenderName());
        System.out.println("message: " + chatMessage.getMessage());
        
        // timestamp가 없으면 현재 시간으로 설정
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(System.currentTimeMillis());
        }
        
        try {
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            System.out.println("✅ 채팅 메시지 저장 완료: ID=" + savedMessage.getId());
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            System.err.println("❌ 채팅 메시지 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 채팅 메시지 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatMessage(@PathVariable Long id) {
        chatMessageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

