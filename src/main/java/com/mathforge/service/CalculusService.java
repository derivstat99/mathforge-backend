package com.mathforge.service;

import com.mathforge.dto.request.CalculusRequest;
import com.mathforge.dto.response.CalculusResponse;

public interface CalculusService {
    CalculusResponse compute(CalculusRequest request);
}
