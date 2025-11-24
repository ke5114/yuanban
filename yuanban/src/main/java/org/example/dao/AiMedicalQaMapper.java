package org.example.dao;

import org.example.model.AiMedicalQa;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AiMedicalQaMapper {

    @Insert("INSERT INTO ai_medical_qa(user_id, conversation_id, question, qa_status, deleted) " +
            "VALUES(#{userId}, #{conversationId}, #{question}, #{qaStatus}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiMedicalQa aiMedicalQa);

    @Update("UPDATE ai_medical_qa SET answer = #{answer}, qa_status = #{qaStatus} WHERE id = #{id}")
    int updateAnswerAndStatus(AiMedicalQa aiMedicalQa);

    @Select("SELECT * FROM ai_medical_qa WHERE user_id = #{userId} AND conversation_id = #{conversationId} AND deleted = 0 ORDER BY create_time ASC")
    List<AiMedicalQa> selectByUserIdAndConversationId(@Param("userId") Long userId, @Param("conversationId") String conversationId);
}