package com.mathforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private String operation;
    private String summary;
    private String steps;

    private Map<String, Double> descriptiveStats;

    private List<GraphPoint> graphPoints;
    private String graphType;
    private String xLabel;
    private String yLabel;

    private Double regressionSlope;
    private Double regressionIntercept;
    private Double rSquared;
    private List<GraphPoint> regressionLine;

    private Double testStatistic;
    private Double pValue;
    private Double criticalValue;
    private Boolean rejectNull;
    private String conclusion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphPoint {
        private Double x;
        private Double y;
        private String label;
    }
}
