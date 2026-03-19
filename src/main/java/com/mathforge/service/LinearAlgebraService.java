package com.mathforge.service;

import com.mathforge.dto.request.LinearAlgebraRequest;
import com.mathforge.dto.response.LinearAlgebraResponse;

public interface LinearAlgebraService {
    LinearAlgebraResponse compute(LinearAlgebraRequest request);
}
