package com.mathforge.service;

import com.mathforge.dto.response.HistoryResponse;
import com.mathforge.dto.response.UserProfileResponse;
import com.mathforge.entity.CalculationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse getProfile(String email);
    Page<HistoryResponse> getHistory(String email, Pageable pageable);
    void saveHistory(String email, CalculationHistory.MathModule module, String input, String result);
}
