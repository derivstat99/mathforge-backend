package com.mathforge.service;

import com.mathforge.dto.request.StatisticsRequest;
import com.mathforge.dto.response.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse compute(StatisticsRequest request);
}
