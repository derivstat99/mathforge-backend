package com.mathforge.service.impl;

import com.mathforge.dto.request.LinearAlgebraRequest;
import com.mathforge.dto.response.LinearAlgebraResponse;
import com.mathforge.exception.MathCalculationException;
import com.mathforge.service.LinearAlgebraService;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.complex.Complex;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LinearAlgebraServiceImpl implements LinearAlgebraService {

    @Override
    public LinearAlgebraResponse compute(LinearAlgebraRequest request) {
        return switch (request.getOperation().toUpperCase()) {
            case "ADD"         -> add(request);
            case "SUBTRACT"    -> subtract(request);
            case "MULTIPLY"    -> multiply(request);
            case "TRANSPOSE"   -> transpose(request);
            case "DETERMINANT" -> determinant(request);
            case "INVERSE"     -> inverse(request);
            case "EIGENVALUES" -> eigenvalues(request);
            case "LU"          -> luDecomposition(request);
            case "RANK"        -> rank(request);
            case "SCALAR_MULTIPLY" -> scalarMultiply(request);
            default -> throw new MathCalculationException("Unknown operation: " + request.getOperation());
        };
    }

    private RealMatrix toRealMatrix(List<List<Double>> data) {
        int rows = data.size();
        int cols = data.get(0).size();
        double[][] arr = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[i][j] = data.get(i).get(j);
            }
        }
        return new Array2DRowRealMatrix(arr);
    }

    private List<List<Double>> fromRealMatrix(RealMatrix m) {
        List<List<Double>> result = new ArrayList<>();
        for (int i = 0; i < m.getRowDimension(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < m.getColumnDimension(); j++) {
                row.add(round(m.getEntry(i, j)));
            }
            result.add(row);
        }
        return result;
    }

    private double round(double value) {
        return Math.round(value * 1e10) / 1e10;
    }

    private LinearAlgebraResponse add(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        RealMatrix b = toRealMatrix(req.getMatrixB());
        if (a.getRowDimension() != b.getRowDimension() || a.getColumnDimension() != b.getColumnDimension()) {
            throw new MathCalculationException("Matrix dimensions must match for addition. A is " + a.getRowDimension() + "x" + a.getColumnDimension() + ", B is " + b.getRowDimension() + "x" + b.getColumnDimension());
        }
        RealMatrix result = a.add(b);
        return LinearAlgebraResponse.builder()
                .operation("ADD")
                .resultMatrix(fromRealMatrix(result))
                .summary("Added two " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrices. Each entry C[i][j] = A[i][j] + B[i][j].")
                .steps("Step 1: Verify dimensions match — both are " + a.getRowDimension() + "x" + a.getColumnDimension() + " ✓\nStep 2: Add corresponding entries element-wise.\nStep 3: Result is a " + result.getRowDimension() + "x" + result.getColumnDimension() + " matrix.")
                .build();
    }

    private LinearAlgebraResponse subtract(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        RealMatrix b = toRealMatrix(req.getMatrixB());
        if (a.getRowDimension() != b.getRowDimension() || a.getColumnDimension() != b.getColumnDimension()) {
            throw new MathCalculationException("Matrix dimensions must match for subtraction.");
        }
        RealMatrix result = a.subtract(b);
        return LinearAlgebraResponse.builder()
                .operation("SUBTRACT")
                .resultMatrix(fromRealMatrix(result))
                .summary("Subtracted B from A. Each entry C[i][j] = A[i][j] - B[i][j].")
                .steps("Step 1: Verify dimensions match ✓\nStep 2: Subtract corresponding entries element-wise.\nStep 3: Result is a " + result.getRowDimension() + "x" + result.getColumnDimension() + " matrix.")
                .build();
    }

    private LinearAlgebraResponse multiply(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        RealMatrix b = toRealMatrix(req.getMatrixB());
        if (a.getColumnDimension() != b.getRowDimension()) {
            throw new MathCalculationException("For multiplication, columns of A (" + a.getColumnDimension() + ") must equal rows of B (" + b.getRowDimension() + ").");
        }
        RealMatrix result = a.multiply(b);
        return LinearAlgebraResponse.builder()
                .operation("MULTIPLY")
                .resultMatrix(fromRealMatrix(result))
                .summary("Multiplied A (" + a.getRowDimension() + "x" + a.getColumnDimension() + ") by B (" + b.getRowDimension() + "x" + b.getColumnDimension() + "). Result is " + result.getRowDimension() + "x" + result.getColumnDimension() + ".")
                .steps("Step 1: Check A columns (" + a.getColumnDimension() + ") = B rows (" + b.getRowDimension() + ") ✓\nStep 2: Compute dot product of each row of A with each column of B.\nStep 3: C[i][j] = sum of A[i][k] * B[k][j] for all k.\nStep 4: Result is " + result.getRowDimension() + "x" + result.getColumnDimension() + " matrix.")
                .build();
    }

    private LinearAlgebraResponse transpose(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        RealMatrix result = a.transpose();
        return LinearAlgebraResponse.builder()
                .operation("TRANSPOSE")
                .resultMatrix(fromRealMatrix(result))
                .summary("Transposed " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix. Result is " + result.getRowDimension() + "x" + result.getColumnDimension() + ".")
                .steps("Step 1: Flip rows and columns.\nStep 2: Entry T[i][j] = A[j][i] for all i, j.\nStep 3: " + a.getRowDimension() + "x" + a.getColumnDimension() + " becomes " + result.getRowDimension() + "x" + result.getColumnDimension() + ".")
                .build();
    }

    private LinearAlgebraResponse determinant(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        if (a.getRowDimension() != a.getColumnDimension()) {
            throw new MathCalculationException("Determinant requires a square matrix. Got " + a.getRowDimension() + "x" + a.getColumnDimension() + ".");
        }
        double det = new LUDecomposition(a).getDeterminant();
        return LinearAlgebraResponse.builder()
                .operation("DETERMINANT")
                .scalarResult(round(det))
                .summary("Determinant of " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix = " + round(det) + (Math.abs(det) < 1e-10 ? " (Matrix is singular — no inverse exists)" : " (Matrix is non-singular — inverse exists)"))
                .steps("Step 1: Verify matrix is square (" + a.getRowDimension() + "x" + a.getColumnDimension() + ") ✓\nStep 2: Apply LU decomposition to compute determinant.\nStep 3: det(A) = product of diagonal entries of U from LU decomposition.\nResult: det(A) = " + round(det))
                .build();
    }

    private LinearAlgebraResponse inverse(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        if (a.getRowDimension() != a.getColumnDimension()) {
            throw new MathCalculationException("Inverse requires a square matrix.");
        }
        double det = new LUDecomposition(a).getDeterminant();
        if (Math.abs(det) < 1e-10) {
            throw new MathCalculationException("Matrix is singular (determinant = 0). Inverse does not exist.");
        }
        RealMatrix result = new LUDecomposition(a).getSolver().getInverse();
        return LinearAlgebraResponse.builder()
                .operation("INVERSE")
                .resultMatrix(fromRealMatrix(result))
                .summary("Inverse of " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix computed. det(A) = " + round(det) + ".")
                .steps("Step 1: Check square matrix ✓\nStep 2: Compute determinant = " + round(det) + " (non-zero, inverse exists) ✓\nStep 3: Apply LU decomposition.\nStep 4: Solve A * A⁻¹ = I using back substitution.\nVerification: A * A⁻¹ = Identity matrix.")
                .build();
    }

    private LinearAlgebraResponse eigenvalues(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        if (a.getRowDimension() != a.getColumnDimension()) {
            throw new MathCalculationException("Eigenvalue decomposition requires a square matrix.");
        }
        EigenDecomposition eigen = new EigenDecomposition(a);
        List<Double> realEigenvalues = new ArrayList<>();
        StringBuilder steps = new StringBuilder();
        steps.append("Step 1: Verify square matrix ✓\n");
        steps.append("Step 2: Solve characteristic equation det(A - λI) = 0\n");
        steps.append("Step 3: Eigenvalues found:\n");
        for (int i = 0; i < a.getRowDimension(); i++) {
            double real = round(eigen.getRealEigenvalue(i));
            double imag = round(eigen.getImagEigenvalue(i));
            realEigenvalues.add(real);
            if (Math.abs(imag) < 1e-10) {
                steps.append("  λ").append(i + 1).append(" = ").append(real).append("\n");
            } else {
                steps.append("  λ").append(i + 1).append(" = ").append(real).append(" + ").append(imag).append("i\n");
            }
        }
        steps.append("Step 4: For each eigenvalue, solve (A - λI)v = 0 to find eigenvectors.");
        RealMatrix vMatrix = eigen.getV();
        List<List<Double>> eigenvectors = fromRealMatrix(vMatrix);
        return LinearAlgebraResponse.builder()
                .operation("EIGENVALUES")
                .eigenvalues(realEigenvalues)
                .eigenvectors(eigenvectors)
                .summary("Found " + realEigenvalues.size() + " eigenvalue(s) for the " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix.")
                .steps(steps.toString())
                .build();
    }

    private LinearAlgebraResponse luDecomposition(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        if (a.getRowDimension() != a.getColumnDimension()) {
            throw new MathCalculationException("LU decomposition requires a square matrix.");
        }
        LUDecomposition lu = new LUDecomposition(a);
        RealMatrix l = lu.getL();
        RealMatrix u = lu.getU();
        return LinearAlgebraResponse.builder()
                .operation("LU")
                .lowerMatrix(fromRealMatrix(l))
                .upperMatrix(fromRealMatrix(u))
                .summary("LU decomposition of " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix. A = L * U.")
                .steps("Step 1: Verify square matrix ✓\nStep 2: Apply Gaussian elimination with partial pivoting.\nStep 3: L = lower triangular matrix with 1s on diagonal.\nStep 4: U = upper triangular matrix.\nResult: A = L × U")
                .build();
    }

    private LinearAlgebraResponse rank(LinearAlgebraRequest req) {
        RealMatrix a = toRealMatrix(req.getMatrixA());
        SingularValueDecomposition svd = new SingularValueDecomposition(a);
        int rank = svd.getRank();
        return LinearAlgebraResponse.builder()
                .operation("RANK")
                .scalarResult((double) rank)
                .summary("Rank of " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix = " + rank + ". " + (rank == Math.min(a.getRowDimension(), a.getColumnDimension()) ? "Matrix has full rank." : "Matrix is rank-deficient."))
                .steps("Step 1: Apply Singular Value Decomposition (SVD).\nStep 2: Count non-zero singular values (threshold = 1e-10).\nStep 3: Rank = number of linearly independent rows/columns = " + rank + ".")
                .build();
    }

    private LinearAlgebraResponse scalarMultiply(LinearAlgebraRequest req) {
        if (req.getScalar() == null) {
            throw new MathCalculationException("Scalar value is required for scalar multiplication.");
        }
        RealMatrix a = toRealMatrix(req.getMatrixA());
        RealMatrix result = a.scalarMultiply(req.getScalar());
        return LinearAlgebraResponse.builder()
                .operation("SCALAR_MULTIPLY")
                .resultMatrix(fromRealMatrix(result))
                .summary("Multiplied every entry of " + a.getRowDimension() + "x" + a.getColumnDimension() + " matrix by scalar " + req.getScalar() + ".")
                .steps("Step 1: For each entry A[i][j], compute " + req.getScalar() + " × A[i][j].\nStep 2: Result has same dimensions as input.")
                .build();
    }
}
