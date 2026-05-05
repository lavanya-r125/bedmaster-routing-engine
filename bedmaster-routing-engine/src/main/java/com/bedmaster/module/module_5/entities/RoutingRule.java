package com.bedmaster.module.module_5.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "routing_rule")
@Data
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ruleId;

    private String ruleType;      // Isolation / Gender / Specialty / Device / Age / Lama

    private String expressionJson; // rule logic stored as JSON

    private Integer priority;      // higher number = checked first

    private String status;         // active / inactive
}