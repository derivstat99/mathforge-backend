package com.mathforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinearAlgebraResponse {

    private String operation;
    private List<List<Double>> resultMatrix;
    private Double scalarResult;
    private List<Double> eigenvalues;
    private List<List<Double>> eigenvectors;
    private List<List<Double>> lowerMatrix;
    private List<List<Double>> upperMatrix;
    private String steps;
    private String summary;
}
