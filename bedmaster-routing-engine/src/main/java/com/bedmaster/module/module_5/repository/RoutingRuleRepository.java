package com.bedmaster.module.module_5.repository;

import com.bedmaster.module.module_5.entities.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Integer> {

    // fetches all active rules sorted by priority (highest first)
    List<RoutingRule> findByStatusOrderByPriorityDesc(String status);
}