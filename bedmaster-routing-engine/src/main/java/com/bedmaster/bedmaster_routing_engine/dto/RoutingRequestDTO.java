package com.bedmaster.bedmaster_routing_engine.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoutingRequestDTO {

    private String patientGender;        // "Male" or "Female"
    private String isolationFlag;        // "Airborne", "Droplet", "Contact", "None"
    private String deviceFlag;           // "Bariatric", "Telemetry", "Crib", "None"
    private String lamaFlag;             // "LAMA" or "None"
    private Integer patientAge;          // age in years, can be null
    private String requestedSpeciality;  // "ICU", "MedSurg", etc. can be null
    private List<BedCandidateDTO> availableBeds;
}