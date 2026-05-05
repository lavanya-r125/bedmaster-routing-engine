package com.bedmaster.module.module_5.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.util.List;

@Data
public class BedCandidateDTO {

    private Long bedId;
    private List<String> attributes;      // e.g. ["NegativePressure", "BariatricBed"]
    private String currentRoomPolicy;     // "Male", "Female", or "Any"

    @JsonAlias("bedType")                 // accepts both "unitType" and "bedType" from Hari
    private String unitType;              // "ICU", "MedSurg", "Pediatric", etc.
}