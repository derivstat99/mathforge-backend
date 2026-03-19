package com.mathforge.service.impl;

import com.mathforge.dto.response.HistoryResponse;
import com.mathforge.dto.response.UserProfileResponse;
import com.mathforge.entity.CalculationHistory;
import com.mathforge.entity.CalculationHistory.MathModule;
import com.mathforge.entity.User;
import com.mathforge.exception.ResourceNotFoundException;
import com.mathforge.repository.CalculationHistoryRepository;
import com.mathforge.repository.SavedGraphRepository;
import com.mathforge.repository.UserRepository;
import com.mathforge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CalculationHistoryRepository historyRepository;
    private final SavedGraphRepository savedGraphRepository;

    @Override
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long totalCalcs = historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged()).getTotalElements();
        long savedGraphs = savedGraphRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .totalCalculations(totalCalcs)
                .savedGraphs(savedGraphs)
                .build();
    }

    @Override
    public Page<HistoryResponse> getHistory(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return historyRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(h -> HistoryResponse.builder()
                        .id(h.getId())
                        .module(h.getModule())
                        .input(h.getInput())
                        .result(h.getResult())
                        .createdAt(h.getCreatedAt())
                        .build());
    }

    @Override
    @Transactional
    public void saveHistory(String email, MathModule module, String input, String result) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CalculationHistory history = CalculationHistory.builder()
                .user(user)
                .module(module)
                .input(input)
                .result(result)
                .build();

        historyRepository.save(history);
    }
}
