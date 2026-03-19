package com.mathforge.service.impl;

import com.mathforge.dto.request.StatisticsRequest;
import com.mathforge.dto.response.StatisticsResponse;
import com.mathforge.dto.response.StatisticsResponse.GraphPoint;
import com.mathforge.exception.MathCalculationException;
import com.mathforge.service.StatisticsService;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public StatisticsResponse compute(StatisticsRequest request) {
        return switch (request.getOperation().toUpperCase()) {
            case "DESCRIPTIVE"    -> descriptive(request);
            case "DISTRIBUTION"   -> distribution(request);
            case "REGRESSION"     -> regression(request);
            case "HYPOTHESIS"     -> hypothesis(request);
            case "CORRELATION"    -> correlation(request);
            case "HISTOGRAM"      -> histogram(request);
            default -> throw new MathCalculationException("Unknown operation: " + request.getOperation());
        };
    }

    private double[] toArray(List<Double> list) {
        if (list == null || list.isEmpty()) {
            throw new MathCalculationException("Data is required for this operation.");
        }
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double round(double v) {
        return Math.round(v * 1e6) / 1e6;
    }

    private StatisticsResponse descriptive(StatisticsRequest req) {
        double[] data = toArray(req.getDataX());
        DescriptiveStatistics ds = new DescriptiveStatistics(data);

        double[] sorted = Arrays.copyOf(data, data.length);
        Arrays.sort(sorted);

        Map<String, Double> stats = new LinkedHashMap<>();
        stats.put("count",      (double) data.length);
        stats.put("mean",       round(ds.getMean()));
        stats.put("median",     round(ds.getPercentile(50)));
        stats.put("mode",       round(computeMode(data)));
        stats.put("variance",   round(ds.getVariance()));
        stats.put("stdDev",     round(ds.getStandardDeviation()));
        stats.put("min",        round(ds.getMin()));
        stats.put("max",        round(ds.getMax()));
        stats.put("range",      round(ds.getMax() - ds.getMin()));
        stats.put("q1",         round(ds.getPercentile(25)));
        stats.put("q3",         round(ds.getPercentile(75)));
        stats.put("iqr",        round(ds.getPercentile(75) - ds.getPercentile(25)));
        stats.put("skewness",   round(ds.getSkewness()));
        stats.put("kurtosis",   round(ds.getKurtosis()));

        List<GraphPoint> boxPlotPoints = List.of(
            GraphPoint.builder().x(0.0).y(stats.get("min")).label("Min").build(),
            GraphPoint.builder().x(1.0).y(stats.get("q1")).label("Q1").build(),
            GraphPoint.builder().x(2.0).y(stats.get("median")).label("Median").build(),
            GraphPoint.builder().x(3.0).y(stats.get("q3")).label("Q3").build(),
            GraphPoint.builder().x(4.0).y(stats.get("max")).label("Max").build()
        );

        return StatisticsResponse.builder()
                .operation("DESCRIPTIVE")
                .descriptiveStats(stats)
                .graphPoints(boxPlotPoints)
                .graphType("BOX_PLOT")
                .xLabel("Statistic")
                .yLabel("Value")
                .steps(buildDescriptiveSteps(stats, data.length))
                .summary("Descriptive statistics for " + data.length + " data points. Mean=" + stats.get("mean") + ", StdDev=" + stats.get("stdDev") + ", Median=" + stats.get("median"))
                .build();
    }

    private double computeMode(double[] data) {
        Map<Double, Long> freq = Arrays.stream(data).boxed()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        return freq.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(Double.NaN);
    }

    private String buildDescriptiveSteps(Map<String, Double> stats, int n) {
        return "Step 1: Sort data and count n = " + n + "\n" +
               "Step 2: Mean = Σx / n = " + stats.get("mean") + "\n" +
               "Step 3: Median = middle value (after sorting) = " + stats.get("median") + "\n" +
               "Step 4: Variance = Σ(x - mean)² / (n-1) = " + stats.get("variance") + "\n" +
               "Step 5: Std Dev = √Variance = " + stats.get("stdDev") + "\n" +
               "Step 6: Q1 = 25th percentile = " + stats.get("q1") + "\n" +
               "Step 7: Q3 = 75th percentile = " + stats.get("q3") + "\n" +
               "Step 8: IQR = Q3 - Q1 = " + stats.get("iqr") + "\n" +
               "Step 9: Skewness = " + stats.get("skewness") + (stats.get("skewness") > 0 ? " (right-skewed)" : stats.get("skewness") < 0 ? " (left-skewed)" : " (symmetric)");
    }

    private StatisticsResponse distribution(StatisticsRequest req) {
        String dist = req.getDistribution() != null ? req.getDistribution().toUpperCase() : "NORMAL";
        double p1 = req.getParam1() != null ? req.getParam1() : 0.0;
        double p2 = req.getParam2() != null ? req.getParam2() : 1.0;

        List<GraphPoint> pdfPoints = new ArrayList<>();
        List<GraphPoint> cdfPoints = new ArrayList<>();
        String steps;
        String summary;

        switch (dist) {
            case "NORMAL" -> {
                NormalDistribution nd = new NormalDistribution(p1, p2);
                double start = p1 - 4 * p2, end = p1 + 4 * p2, step = (end - start) / 100;
                for (double x = start; x <= end; x += step) {
                    pdfPoints.add(GraphPoint.builder().x(round(x)).y(round(nd.density(x))).build());
                    cdfPoints.add(GraphPoint.builder().x(round(x)).y(round(nd.cumulativeProbability(x))).build());
                }
                steps = "Normal Distribution N(μ=" + p1 + ", σ=" + p2 + ")\n" +
                        "PDF: f(x) = (1/σ√2π) * e^(-(x-μ)²/2σ²)\n" +
                        "Mean = " + p1 + ", Variance = " + round(p2 * p2) + "\n" +
                        "P(X < μ) = 0.5 (symmetric)\n" +
                        "68-95-99.7 rule: ±1σ covers 68.27%, ±2σ covers 95.45%, ±3σ covers 99.73%";
                summary = "Normal distribution with μ=" + p1 + ", σ=" + p2;
            }
            case "BINOMIAL" -> {
                int n = (int) p1;
                double p = p2;
                BinomialDistribution bd = new BinomialDistribution(n, p);
                for (int k = 0; k <= n; k++) {
                    pdfPoints.add(GraphPoint.builder().x((double) k).y(round(bd.probability(k))).label("k=" + k).build());
                    cdfPoints.add(GraphPoint.builder().x((double) k).y(round(bd.cumulativeProbability(k))).label("k=" + k).build());
                }
                steps = "Binomial Distribution B(n=" + n + ", p=" + p + ")\n" +
                        "P(X=k) = C(n,k) * p^k * (1-p)^(n-k)\n" +
                        "Mean = np = " + round(n * p) + "\n" +
                        "Variance = np(1-p) = " + round(n * p * (1 - p)) + "\n" +
                        "Std Dev = " + round(Math.sqrt(n * p * (1 - p)));
                summary = "Binomial distribution with n=" + n + ", p=" + p + ". Mean=" + round(n * p);
            }
            case "POISSON" -> {
                double lambda = p1;
                PoissonDistribution pd = new PoissonDistribution(lambda);
                int maxK = (int) (lambda + 4 * Math.sqrt(lambda)) + 1;
                for (int k = 0; k <= maxK; k++) {
                    pdfPoints.add(GraphPoint.builder().x((double) k).y(round(pd.probability(k))).label("k=" + k).build());
                    cdfPoints.add(GraphPoint.builder().x((double) k).y(round(pd.cumulativeProbability(k))).label("k=" + k).build());
                }
                steps = "Poisson Distribution P(λ=" + lambda + ")\n" +
                        "P(X=k) = (λ^k * e^(-λ)) / k!\n" +
                        "Mean = Variance = λ = " + lambda + "\n" +
                        "Std Dev = √λ = " + round(Math.sqrt(lambda));
                summary = "Poisson distribution with λ=" + lambda + ". Mean=Variance=" + lambda;
            }
            case "EXPONENTIAL" -> {
                double rate = p1 > 0 ? p1 : 1.0;
                ExponentialDistribution ed = new ExponentialDistribution(1.0 / rate);
                for (double x = 0; x <= 5.0 / rate; x += (5.0 / rate) / 100) {
                    pdfPoints.add(GraphPoint.builder().x(round(x)).y(round(ed.density(x))).build());
                    cdfPoints.add(GraphPoint.builder().x(round(x)).y(round(ed.cumulativeProbability(x))).build());
                }
                steps = "Exponential Distribution Exp(λ=" + rate + ")\n" +
                        "PDF: f(x) = λe^(-λx) for x ≥ 0\n" +
                        "Mean = 1/λ = " + round(1.0 / rate) + "\n" +
                        "Variance = 1/λ² = " + round(1.0 / (rate * rate));
                summary = "Exponential distribution with rate λ=" + rate + ". Mean=" + round(1.0 / rate);
            }
            default -> throw new MathCalculationException("Unknown distribution: " + dist + ". Supported: NORMAL, BINOMIAL, POISSON, EXPONENTIAL");
        }

        return StatisticsResponse.builder()
                .operation("DISTRIBUTION")
                .graphPoints(pdfPoints)
                .graphType("LINE")
                .xLabel("x")
                .yLabel("P(X=x) or f(x)")
                .steps(steps)
                .summary(summary)
                .build();
    }

    private StatisticsResponse regression(StatisticsRequest req) {
        double[] x = toArray(req.getDataX());
        double[] y = toArray(req.getDataY());
        if (x.length != y.length) {
            throw new MathCalculationException("X and Y must have the same number of data points.");
        }

        SimpleRegression reg = new SimpleRegression();
        for (int i = 0; i < x.length; i++) reg.addData(x[i], y[i]);

        double slope = round(reg.getSlope());
        double intercept = round(reg.getIntercept());
        double r2 = round(reg.getRSquare());
        double r = round(reg.getR());

        List<GraphPoint> scatter = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            scatter.add(GraphPoint.builder().x(x[i]).y(y[i]).build());
        }

        double xMin = Arrays.stream(x).min().orElse(0);
        double xMax = Arrays.stream(x).max().orElse(1);
        List<GraphPoint> line = List.of(
            GraphPoint.builder().x(xMin).y(round(slope * xMin + intercept)).build(),
            GraphPoint.builder().x(xMax).y(round(slope * xMax + intercept)).build()
        );

        return StatisticsResponse.builder()
                .operation("REGRESSION")
                .graphPoints(scatter)
                .regressionLine(line)
                .regressionSlope(slope)
                .regressionIntercept(intercept)
                .rSquared(r2)
                .graphType("SCATTER")
                .xLabel("X")
                .yLabel("Y")
                .steps(
                    "Step 1: n = " + x.length + " data points\n" +
                    "Step 2: Compute means: x̄ = " + round(StatUtils.mean(x)) + ", ȳ = " + round(StatUtils.mean(y)) + "\n" +
                    "Step 3: Slope b₁ = Σ(xi-x̄)(yi-ȳ) / Σ(xi-x̄)² = " + slope + "\n" +
                    "Step 4: Intercept b₀ = ȳ - b₁x̄ = " + intercept + "\n" +
                    "Step 5: Regression line: ŷ = " + slope + "x + " + intercept + "\n" +
                    "Step 6: R² = " + r2 + " (" + round(r2 * 100) + "% variance explained)\n" +
                    "Step 7: Pearson r = " + r + " (" + interpretCorrelation(r) + ")"
                )
                .summary("Linear regression: ŷ = " + slope + "x + " + intercept + ". R²=" + r2 + " (" + interpretCorrelation(r) + " correlation)")
                .build();
    }

    private String interpretCorrelation(double r) {
        double abs = Math.abs(r);
        if (abs >= 0.9) return "very strong";
        if (abs >= 0.7) return "strong";
        if (abs >= 0.5) return "moderate";
        if (abs >= 0.3) return "weak";
        return "very weak";
    }

    private StatisticsResponse hypothesis(StatisticsRequest req) {
        double[] data = toArray(req.getDataX());
        double mu0 = req.getTestValue() != null ? req.getTestValue() : 0.0;
        double alpha = req.getAlpha() != null ? req.getAlpha() : 0.05;
        String type = req.getHypothesisType() != null ? req.getHypothesisType().toUpperCase() : "TWO_TAILED";

        TTest tTest = new TTest();
        double tStat = round(computeTStat(data, mu0));
        double pValue;

        switch (type) {
            case "LEFT_TAILED"  -> pValue = round(tTest.tTest(mu0, data) / 2.0);
            case "RIGHT_TAILED" -> pValue = round(1.0 - tTest.tTest(mu0, data) / 2.0);
            default             -> pValue = round(tTest.tTest(mu0, data));
        }

        boolean reject = pValue < alpha;
        DescriptiveStatistics ds = new DescriptiveStatistics(data);

        return StatisticsResponse.builder()
                .operation("HYPOTHESIS")
                .testStatistic(tStat)
                .pValue(pValue)
                .criticalValue(alpha)
                .rejectNull(reject)
                .conclusion(reject
                    ? "Reject H₀ at α=" + alpha + ". Sufficient evidence that mean ≠ " + mu0 + "."
                    : "Fail to reject H₀ at α=" + alpha + ". Insufficient evidence that mean ≠ " + mu0 + ".")
                .steps(
                    "One-sample t-test (" + type.replace("_", " ").toLowerCase() + ")\n" +
                    "H₀: μ = " + mu0 + "\n" +
                    "H₁: μ " + (type.equals("LEFT_TAILED") ? "<" : type.equals("RIGHT_TAILED") ? ">" : "≠") + " " + mu0 + "\n" +
                    "Step 1: n = " + data.length + ", x̄ = " + round(ds.getMean()) + ", s = " + round(ds.getStandardDeviation()) + "\n" +
                    "Step 2: t = (x̄ - μ₀) / (s/√n) = " + tStat + "\n" +
                    "Step 3: Degrees of freedom = " + (data.length - 1) + "\n" +
                    "Step 4: p-value = " + pValue + "\n" +
                    "Step 5: Compare p-value (" + pValue + ") vs α (" + alpha + ")\n" +
                    "Conclusion: " + (reject ? "REJECT H₀" : "FAIL TO REJECT H₀")
                )
                .summary("t=" + tStat + ", p=" + pValue + ". " + (reject ? "Reject H₀." : "Fail to reject H₀."))
                .build();
    }

    private double computeTStat(double[] data, double mu0) {
        DescriptiveStatistics ds = new DescriptiveStatistics(data);
        return (ds.getMean() - mu0) / (ds.getStandardDeviation() / Math.sqrt(data.length));
    }

    private StatisticsResponse correlation(StatisticsRequest req) {
        double[] x = toArray(req.getDataX());
        double[] y = toArray(req.getDataY());
        if (x.length != y.length) throw new MathCalculationException("X and Y must have equal length.");

        PearsonsCorrelation pc = new PearsonsCorrelation();
        double r = round(pc.correlation(x, y));
        double r2 = round(r * r);

        List<GraphPoint> scatter = new ArrayList<>();
        for (int i = 0; i < x.length; i++) {
            scatter.add(GraphPoint.builder().x(x[i]).y(y[i]).build());
        }

        return StatisticsResponse.builder()
                .operation("CORRELATION")
                .graphPoints(scatter)
                .rSquared(r2)
                .graphType("SCATTER")
                .xLabel("X")
                .yLabel("Y")
                .steps(
                    "Step 1: n = " + x.length + " pairs\n" +
                    "Step 2: Pearson r = Σ(xi-x̄)(yi-ȳ) / √[Σ(xi-x̄)² * Σ(yi-ȳ)²]\n" +
                    "Step 3: r = " + r + "\n" +
                    "Step 4: r² = " + r2 + "\n" +
                    "Interpretation: " + interpretCorrelation(r) + " " + (r >= 0 ? "positive" : "negative") + " linear correlation"
                )
                .summary("Pearson r = " + r + " (" + interpretCorrelation(r) + " " + (r >= 0 ? "positive" : "negative") + " correlation). r²=" + r2)
                .build();
    }

    private StatisticsResponse histogram(StatisticsRequest req) {
        double[] data = toArray(req.getDataX());
        int bins = req.getParam1() != null ? req.getParam1().intValue() : 10;
        bins = Math.max(2, Math.min(bins, 50));

        double min = Arrays.stream(data).min().orElse(0);
        double max = Arrays.stream(data).max().orElse(1);
        double binWidth = (max - min) / bins;

        int[] counts = new int[bins];
        for (double v : data) {
            int idx = (int) ((v - min) / binWidth);
            if (idx == bins) idx = bins - 1;
            counts[idx]++;
        }

        List<GraphPoint> points = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            double binStart = round(min + i * binWidth);
            points.add(GraphPoint.builder()
                    .x(round(binStart + binWidth / 2))
                    .y((double) counts[i])
                    .label(String.format("%.2f-%.2f", binStart, binStart + binWidth))
                    .build());
        }

        DescriptiveStatistics ds = new DescriptiveStatistics(data);
        return StatisticsResponse.builder()
                .operation("HISTOGRAM")
                .graphPoints(points)
                .graphType("BAR")
                .xLabel("Value")
                .yLabel("Frequency")
                .steps(
                    "Step 1: Range = max - min = " + round(max - min) + "\n" +
                    "Step 2: Bin width = range / " + bins + " = " + round(binWidth) + "\n" +
                    "Step 3: Count data points in each bin\n" +
                    "Step 4: n = " + data.length + ", mean = " + round(ds.getMean()) + ", std dev = " + round(ds.getStandardDeviation())
                )
                .summary("Histogram of " + data.length + " data points across " + bins + " bins. Range: [" + round(min) + ", " + round(max) + "]")
                .build();
    }
}
