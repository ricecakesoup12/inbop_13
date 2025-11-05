package inbop._group;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String sender; // "user" 또는 "guardian"

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Long timestamp;

    @Column(nullable = false)
    private Instant createdAt;

    public ChatMessage() {
        this.createdAt = Instant.now();
    }

    public ChatMessage(String userId, String sender, String senderName, String message, Long timestamp) {
        this.userId = userId;
        this.sender = sender;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

