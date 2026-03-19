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
public class CalculusResponse {

    private String operation;
    private String input;
    private String result;
    private String resultLatex;
    private String steps;
    private String summary;
    private List<SeriesTerm> seriesTerms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesTerm {
        private int termIndex;
        private String expression;
        private Double numericalValue;
    }
}
