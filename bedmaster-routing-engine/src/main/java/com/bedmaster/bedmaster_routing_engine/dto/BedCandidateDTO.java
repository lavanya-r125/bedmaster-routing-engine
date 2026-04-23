package com.bedmaster.bedmaster_routing_engine.dto;

import lombok.Data;
import java.util.List;

@Data
public class BedCandidateDTO {

    private Long bedId;
    private List<String> attributes;      // e.g. ["NegativePressure", "BariatricBed"]
    private String currentRoomPolicy;     // "Male", "Female", or "Any"
    private String unitType;              // "ICU", "MedSurg", "Pediatric", etc.
}