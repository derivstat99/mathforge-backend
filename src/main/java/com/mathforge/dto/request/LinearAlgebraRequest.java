package com.mathforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class LinearAlgebraRequest {

    @NotBlank(message = "Operation is required")
    private String operation;

    @NotNull(message = "Matrix A is required")
    private List<List<Double>> matrixA;

    private List<List<Double>> matrixB;

    private Double scalar;
}
