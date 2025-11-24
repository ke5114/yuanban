package org.example.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.AiMedicalQaMapper;
import org.example.model.AiMedicalQa;
import org.example.model.MedicalQaRequest;
import org.example.service.AiMedicalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiMedicalServiceImpl implements AiMedicalService {

    private static final Logger logger = LoggerFactory.getLogger(AiMedicalServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiMedicalQaMapper aiMedicalQaMapper;

    @Autowired
    public AiMedicalServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper, AiMedicalQaMapper aiMedicalQaMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.aiMedicalQaMapper = aiMedicalQaMapper;
    }

    @Override
    public AiMedicalQa getMedicalAnswer(MedicalQaRequest request) {
        String question = request.getQuestion();
        Long userId = request.getUserId();
        String conversationId = request.getConversationId();

        // --- 步骤 1: 生成会话ID并创建数据库记录 ---
        if (conversationId == null || conversationId.trim().isEmpty()) {
            conversationId = UUID.randomUUID().toString();
            logger.info("创建新对话，ID: {}", conversationId);
        } else {
            logger.info("继续对话，ID: {}", conversationId);
        }

        AiMedicalQa qaRecord = new AiMedicalQa();
        qaRecord.setUserId(userId);
        qaRecord.setConversationId(conversationId);
        qaRecord.setQuestion(question);
        qaRecord.setQaStatus(0); // 0: 处理中
        aiMedicalQaMapper.insert(qaRecord);
        logger.debug("已向数据库插入记录，ID: {}", qaRecord.getId());

        // --- 步骤 2: 调用AI接口获取回答 ---
        String aiAnswer;
        try {
            String ollamaUrl = "http://localhost:11434/api/generate";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建多轮对话的上下文
            StringBuilder promptBuilder = new StringBuilder("请以专业医疗顾问的身份，详细解答用户的问题。回答需专业、准确且易于理解。");
            promptBuilder.append("\n用户现在的问题是：").append(question).append("\n");

            // 如果是多轮对话，获取历史记录并拼接
            List<AiMedicalQa> historyRecords = aiMedicalQaMapper.selectByUserIdAndConversationId(userId, conversationId);
            if (historyRecords.size() > 1) { // 大于1是因为我们刚刚插入了一条
                logger.info("为对话 {} 拼接历史上下文，共 {} 条记录", conversationId, historyRecords.size() - 1);
                promptBuilder.insert(0, "以下是用户与你的历史对话，请参考这些信息来回答用户的最新问题：\n");
                // 遍历历史记录，除了最后一条（也就是我们刚插入的这条）
                for (int i = 0; i < historyRecords.size() - 1; i++) {
                    AiMedicalQa history = historyRecords.get(i);
                    promptBuilder.append("用户: ").append(history.getQuestion()).append("\n");
                    promptBuilder.append("医生: ").append(history.getAnswer()).append("\n");
                }
                promptBuilder.append("--- 历史对话结束 ---").append("\n");
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-r1:14b");
            requestBody.put("prompt", promptBuilder.toString());
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            logger.info("向Ollama发送请求: {}", ollamaUrl);
            String response = restTemplate.postForObject(ollamaUrl, requestEntity, String.class);
            logger.debug("收到Ollama响应: {}", response);

            Map<String, Object> responseMap = objectMapper.readValue(response, HashMap.class);
            aiAnswer = (String) responseMap.get("response");
            if (aiAnswer == null) {
                throw new RuntimeException("AI返回结果中未找到 'response' 字段");
            }

        } catch (Exception e) {
            logger.error("调用AI模型或处理响应时发生异常", e);
            qaRecord.setQaStatus(2); // 2: 失败
            qaRecord.setAnswer("很抱歉，AI医疗问答服务暂时不可用，请稍后重试。");
            aiMedicalQaMapper.updateAnswerAndStatus(qaRecord);
            return qaRecord;
        }

        // --- 步骤 3: 更新数据库记录 ---
        qaRecord.setAnswer(aiAnswer);
        qaRecord.setQaStatus(1); // 1: 完成
        aiMedicalQaMapper.updateAnswerAndStatus(qaRecord);
        logger.info("已更新数据库记录，ID: {}", qaRecord.getId());

        return qaRecord;
    }
}