package com.mathforge.controller;

import com.mathforge.dto.request.LinearAlgebraRequest;
import com.mathforge.dto.response.ApiResponse;
import com.mathforge.dto.response.LinearAlgebraResponse;
import com.mathforge.entity.CalculationHistory.MathModule;
import com.mathforge.service.LinearAlgebraService;
import com.mathforge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/math/linear-algebra")
@RequiredArgsConstructor
public class LinearAlgebraController {

    private final LinearAlgebraService linearAlgebraService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<LinearAlgebraResponse>> compute(
            @Valid @RequestBody LinearAlgebraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        LinearAlgebraResponse response = linearAlgebraService.compute(request);

        userService.saveHistory(
                userDetails.getUsername(),
                MathModule.LINEAR_ALGEBRA,
                request.getOperation() + " on " + request.getMatrixA().size() + "x" + request.getMatrixA().get(0).size() + " matrix",
                response.getSummary()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
