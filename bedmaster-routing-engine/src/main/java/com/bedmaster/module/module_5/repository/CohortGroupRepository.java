package com.bedmaster.module.module_5.repository;

import com.bedmaster.module.module_5.entities.CohortGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortGroupRepository extends JpaRepository<CohortGroup, Integer> {
}