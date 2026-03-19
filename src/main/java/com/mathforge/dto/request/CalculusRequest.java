package com.mathforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CalculusRequest {

    @NotBlank(message = "Operation is required")
    private String operation;

    @NotBlank(message = "Expression is required")
    private String expression;

    private String variable;
    private String lowerBound;
    private String upperBound;
    private Integer seriesTerms;
    private String point;
}
