package com.bedmaster.module.module_5.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cohort_group")
@Data
public class CohortGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cohortId;

    private int ruleId;    // which rule this cohort belongs to

    private String label;  // e.g. "Airborne Isolation Cohort"

    @Column(columnDefinition = "TEXT")
    private String notes;  // description of the cohort
}