package org.example.model;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MedicalQaRequest {

    @NotNull(message = "userId字段不能为空")
    private Long userId;

    private String conversationId;

    @NotBlank(message = "question字段不能为空")
    private String question;
}