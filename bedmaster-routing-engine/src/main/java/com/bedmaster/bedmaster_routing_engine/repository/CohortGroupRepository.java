package com.bedmaster.bedmaster_routing_engine.repository;

import com.bedmaster.bedmaster_routing_engine.entities.CohortGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortGroupRepository extends JpaRepository<CohortGroup, Integer> {
}