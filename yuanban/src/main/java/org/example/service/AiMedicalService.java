package org.example.service;

import org.example.model.AiMedicalQa;
import org.example.model.MedicalQaRequest;

public interface AiMedicalService {
    // 只保留这一个方法
    AiMedicalQa getMedicalAnswer(MedicalQaRequest request);
}