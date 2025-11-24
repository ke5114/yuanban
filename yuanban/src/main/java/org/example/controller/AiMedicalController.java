package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.model.AiMedicalQa;
import org.example.model.MedicalQaRequest;
import org.example.service.AiMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/ai/medical")
@Api(tags = "AI医疗问答接口", description = "提供医疗问题的智能问答服务，基于本地DeepSeek模型")
public class AiMedicalController {

    @Autowired
    private AiMedicalService aiMedicalService;

    @PostMapping("/qa")
    @ApiOperation(value = "医疗问题问答", notes = "用户提交医疗问题，获取AI生成的专业回答（支持多轮对话）")
    public ResponseEntity<AiMedicalQa> medicalQa(
            @ApiParam(name = "requestBody", value = "包含用户ID、对话ID和问题的请求体", required = true)
            @Valid
            @RequestBody MedicalQaRequest request) {

        // 错误的调用方式:
        // AiMedicalQa result = aiMedicalService.getMedicalAnswer(String.valueOf(request));

        // 正确的调用方式: 直接传递 request 对象
        AiMedicalQa result = aiMedicalService.getMedicalAnswer(request);

        return ResponseEntity.ok(result);
    }
}