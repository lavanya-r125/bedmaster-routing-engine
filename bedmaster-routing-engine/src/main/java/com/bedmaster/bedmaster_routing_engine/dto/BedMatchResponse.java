package com.bedmaster.bedmaster_routing_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BedMatchResponse {

    private Long bedId;
    private String status;        // "MATCHED", "REJECTED", or "ERROR"
    private String alertMessage;  // reason for rejection or success message
    private String ruleType;      // which rule caused the result
    private int priority;         // priority of the rule that caused the result
}