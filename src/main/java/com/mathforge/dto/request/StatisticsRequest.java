package com.mathforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class StatisticsRequest {

    @NotBlank(message = "Operation is required")
    private String operation;

    private List<Double> dataX;
    private List<Double> dataY;

    private String distribution;
    private Double param1;
    private Double param2;
    private Double testValue;
    private Double alpha;
    private String hypothesisType;
}
