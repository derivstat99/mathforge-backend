package com.mathforge.dto.response;

import com.mathforge.entity.CalculationHistory.MathModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private MathModule module;
    private String input;
    private String result;
    private LocalDateTime createdAt;
}
