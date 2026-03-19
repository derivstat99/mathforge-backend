package com.mathforge.repository;

import com.mathforge.entity.CalculationHistory;
import com.mathforge.entity.CalculationHistory.MathModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculationHistoryRepository extends JpaRepository<CalculationHistory, Long> {

    Page<CalculationHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<CalculationHistory> findByUserIdAndModuleOrderByCreatedAtDesc(Long userId, MathModule module, Pageable pageable);

    void deleteByUserId(Long userId);
}
