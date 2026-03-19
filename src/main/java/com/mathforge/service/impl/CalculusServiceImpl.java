package com.mathforge.service.impl;

import com.mathforge.dto.request.CalculusRequest;
import com.mathforge.dto.response.CalculusResponse;
import com.mathforge.dto.response.CalculusResponse.SeriesTerm;
import com.mathforge.exception.MathCalculationException;
import com.mathforge.service.CalculusService;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculusServiceImpl implements CalculusService {

    @Override
    public CalculusResponse compute(CalculusRequest request) {
        String variable = request.getVariable() != null && !request.getVariable().isBlank()
                ? request.getVariable() : "x";

        return switch (request.getOperation().toUpperCase()) {
            case "DIFFERENTIATE"         -> differentiate(request, variable);
            case "DIFFERENTIATE_NTH"     -> differentiateNth(request, variable);
            case "INTEGRATE_INDEFINITE"  -> integrateIndefinite(request, variable);
            case "INTEGRATE_DEFINITE"    -> integrateDefinite(request, variable);
            case "SERIES"                -> series(request, variable);
            case "SIMPLIFY"              -> simplify(request);
            case "EXPAND"                -> expand(request);
            case "FACTOR"                -> factor(request);
            default -> throw new MathCalculationException("Unknown operation: " + request.getOperation());
        };
    }

    private ExprEvaluator newEvaluator() {
        return new ExprEvaluator(false, (short) 100);
    }

    private String evaluate(ExprEvaluator eval, String expr) {
        try {
            IExpr result = eval.evaluate(expr);
            return result.toString();
        } catch (Exception e) {
            throw new MathCalculationException("Could not evaluate: " + expr + ". Check your expression syntax.");
        }
    }

    private CalculusResponse differentiate(CalculusRequest req, String variable) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String symjaExpr = "D[" + expr + ", " + variable + "]";
        String raw = evaluate(eval, symjaExpr);
        String simplified = evaluate(eval, "Simplify[" + raw + "]");

        return CalculusResponse.builder()
                .operation("DIFFERENTIATE")
                .input(expr)
                .result(simplified)
                .steps(
                    "Step 1: Expression = " + expr + "\n" +
                    "Step 2: Apply differentiation rules with respect to " + variable + "\n" +
                    "Step 3: Raw derivative = " + raw + "\n" +
                    "Step 4: Simplified = " + simplified
                )
                .summary("d/d" + variable + " (" + expr + ") = " + simplified)
                .build();
    }

    private CalculusResponse differentiateNth(CalculusRequest req, String variable) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        int n = req.getSeriesTerms() != null ? req.getSeriesTerms() : 2;
        String symjaExpr = "D[" + expr + ", {" + variable + ", " + n + "}]";
        String raw = evaluate(eval, symjaExpr);
        String simplified = evaluate(eval, "Simplify[" + raw + "]");

        StringBuilder steps = new StringBuilder();
        steps.append("Step 1: Expression = ").append(expr).append("\n");
        for (int i = 1; i <= n; i++) {
            String di = evaluate(eval, "D[" + expr + ", {" + variable + ", " + i + "}]");
            String dsi = evaluate(eval, "Simplify[" + di + "]");
            steps.append("Step ").append(i + 1).append(": d^").append(i).append("/d").append(variable).append("^").append(i).append(" = ").append(dsi).append("\n");
        }

        return CalculusResponse.builder()
                .operation("DIFFERENTIATE_NTH")
                .input(expr)
                .result(simplified)
                .steps(steps.toString())
                .summary("d^" + n + "/d" + variable + "^" + n + " (" + expr + ") = " + simplified)
                .build();
    }

    private CalculusResponse integrateIndefinite(CalculusRequest req, String variable) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String symjaExpr = "Integrate[" + expr + ", " + variable + "]";
        String raw = evaluate(eval, symjaExpr);
        String simplified = evaluate(eval, "Simplify[" + raw + "]");

        return CalculusResponse.builder()
                .operation("INTEGRATE_INDEFINITE")
                .input(expr)
                .result(simplified + " + C")
                .steps(
                    "Step 1: Expression = " + expr + "\n" +
                    "Step 2: Apply integration rules with respect to " + variable + "\n" +
                    "Step 3: Raw antiderivative = " + raw + "\n" +
                    "Step 4: Simplified = " + simplified + "\n" +
                    "Step 5: Add constant of integration C"
                )
                .summary("∫ (" + expr + ") d" + variable + " = " + simplified + " + C")
                .build();
    }

    private CalculusResponse integrateDefinite(CalculusRequest req, String variable) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String lower = req.getLowerBound() != null ? req.getLowerBound() : "0";
        String upper = req.getUpperBound() != null ? req.getUpperBound() : "1";

        String symjaExpr = "Integrate[" + expr + ", {" + variable + ", " + lower + ", " + upper + "}]";
        String raw = evaluate(eval, symjaExpr);
        String simplified = evaluate(eval, "Simplify[" + raw + "]");
        String numerical = evaluate(eval, "N[" + simplified + "]");

        return CalculusResponse.builder()
                .operation("INTEGRATE_DEFINITE")
                .input(expr)
                .result(simplified)
                .steps(
                    "Step 1: Expression = " + expr + "\n" +
                    "Step 2: Find antiderivative F(" + variable + ") = " + evaluate(eval, "Simplify[Integrate[" + expr + ", " + variable + "]]") + "\n" +
                    "Step 3: Apply fundamental theorem: F(" + upper + ") - F(" + lower + ")\n" +
                    "Step 4: Exact result = " + simplified + "\n" +
                    "Step 5: Numerical approximation ≈ " + numerical
                )
                .summary("∫ from " + lower + " to " + upper + " of (" + expr + ") d" + variable + " = " + simplified + " ≈ " + numerical)
                .build();
    }

    private CalculusResponse series(CalculusRequest req, String variable) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String point = req.getPoint() != null ? req.getPoint() : "0";
        int terms = req.getSeriesTerms() != null ? req.getSeriesTerms() : 5;

        String symjaExpr = "Series[" + expr + ", {" + variable + ", " + point + ", " + (terms - 1) + "}]";
        String seriesResult = evaluate(eval, symjaExpr);
        String normal = evaluate(eval, "Normal[" + seriesResult + "]");

        List<SeriesTerm> termList = new ArrayList<>();
        for (int i = 0; i < terms; i++) {
            String termExpr = "SeriesCoefficient[" + expr + ", {" + variable + ", " + point + ", " + i + "}]";
            try {
                String coeff = evaluate(eval, termExpr);
                String numerical = evaluate(eval, "N[" + coeff + "]");
                double numVal = 0;
                try { numVal = Double.parseDouble(numerical); } catch (Exception ignored) {}
                termList.add(SeriesTerm.builder()
                        .termIndex(i)
                        .expression("(" + coeff + ") * (" + variable + " - " + point + ")^" + i)
                        .numericalValue(numVal)
                        .build());
            } catch (Exception ignored) {}
        }

        return CalculusResponse.builder()
                .operation("SERIES")
                .input(expr)
                .result(normal)
                .seriesTerms(termList)
                .steps(
                    "Step 1: Expand " + expr + " around " + variable + " = " + point + "\n" +
                    "Step 2: Compute Taylor/Maclaurin coefficients a_n = f^(n)(x0) / n!\n" +
                    "Step 3: Series = Σ a_n * (" + variable + " - " + point + ")^n\n" +
                    "Step 4: First " + terms + " terms computed\n" +
                    "Result: " + normal
                )
                .summary("Taylor series of " + expr + " around " + variable + " = " + point + " up to order " + (terms - 1) + ".")
                .build();
    }

    private CalculusResponse simplify(CalculusRequest req) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String result = evaluate(eval, "Simplify[" + expr + "]");
        return CalculusResponse.builder()
                .operation("SIMPLIFY")
                .input(expr)
                .result(result)
                .steps("Step 1: Input = " + expr + "\nStep 2: Apply algebraic simplification rules.\nResult: " + result)
                .summary("Simplified: " + expr + " → " + result)
                .build();
    }

    private CalculusResponse expand(CalculusRequest req) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String result = evaluate(eval, "Expand[" + expr + "]");
        return CalculusResponse.builder()
                .operation("EXPAND")
                .input(expr)
                .result(result)
                .steps("Step 1: Input = " + expr + "\nStep 2: Distribute and expand all terms.\nResult: " + result)
                .summary("Expanded: " + expr + " → " + result)
                .build();
    }

    private CalculusResponse factor(CalculusRequest req) {
        ExprEvaluator eval = newEvaluator();
        String expr = req.getExpression();
        String result = evaluate(eval, "Factor[" + expr + "]");
        return CalculusResponse.builder()
                .operation("FACTOR")
                .input(expr)
                .result(result)
                .steps("Step 1: Input = " + expr + "\nStep 2: Find common factors and factor completely.\nResult: " + result)
                .summary("Factored: " + expr + " → " + result)
                .build();
    }
}
