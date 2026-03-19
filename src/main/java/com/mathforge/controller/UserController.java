package com.mathforge.controller;

import com.mathforge.dto.response.ApiResponse;
import com.mathforge.dto.response.HistoryResponse;
import com.mathforge.dto.response.UserProfileResponse;
import com.mathforge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<HistoryResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<HistoryResponse> history = userService.getHistory(
                userDetails.getUsername(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
