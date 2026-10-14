package com.hireflow.backend.repository;

import com.hireflow.backend.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId(Long applicationId);

    List<Interview> findByApplicationIdIn(Collection<Long> applicationIds);

    void deleteByApplicationId(Long applicationId);
}
