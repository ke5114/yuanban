package org.example.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiMedicalQa {
    private Long id;
    private Long userId;
    private String conversationId;
    private String question;
    private String answer;
    private Integer qaStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}