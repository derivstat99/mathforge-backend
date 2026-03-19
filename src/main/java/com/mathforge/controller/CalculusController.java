package com.mathforge.controller;

import com.mathforge.dto.request.CalculusRequest;
import com.mathforge.dto.response.ApiResponse;
import com.mathforge.dto.response.CalculusResponse;
import com.mathforge.entity.CalculationHistory.MathModule;
import com.mathforge.service.CalculusService;
import com.mathforge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/math/calculus")
@RequiredArgsConstructor
public class CalculusController {

    private final CalculusService calculusService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<CalculusResponse>> compute(
            @Valid @RequestBody CalculusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CalculusResponse response = calculusService.compute(request);

        userService.saveHistory(
                userDetails.getUsername(),
                MathModule.CALCULUS,
                request.getOperation() + ": " + request.getExpression(),
                response.getSummary()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
