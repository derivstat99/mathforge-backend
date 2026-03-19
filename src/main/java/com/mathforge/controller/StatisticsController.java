package com.mathforge.controller;

import com.mathforge.dto.request.StatisticsRequest;
import com.mathforge.dto.response.ApiResponse;
import com.mathforge.dto.response.StatisticsResponse;
import com.mathforge.entity.CalculationHistory.MathModule;
import com.mathforge.service.StatisticsService;
import com.mathforge.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/math/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> compute(
            @Valid @RequestBody StatisticsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        StatisticsResponse response = statisticsService.compute(request);

        userService.saveHistory(
                userDetails.getUsername(),
                MathModule.STATISTICS,
                request.getOperation() + (request.getDataX() != null ? " on " + request.getDataX().size() + " points" : ""),
                response.getSummary()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
