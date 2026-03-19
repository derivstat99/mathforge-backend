package com.mathforge.repository;

import com.mathforge.entity.SavedGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SavedGraphRepository extends JpaRepository<SavedGraph, Long> {

    List<SavedGraph> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
