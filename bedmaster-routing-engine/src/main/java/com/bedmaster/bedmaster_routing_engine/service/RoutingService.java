package com.bedmaster.bedmaster_routing_engine.service;

import com.bedmaster.bedmaster_routing_engine.constants.RuleType;
import com.bedmaster.bedmaster_routing_engine.dto.BedCandidateDTO;
import com.bedmaster.bedmaster_routing_engine.dto.BedMatchResponse;
import com.bedmaster.bedmaster_routing_engine.dto.RoutingRequestDTO;
import com.bedmaster.bedmaster_routing_engine.entities.RoutingRule;
import com.bedmaster.bedmaster_routing_engine.repository.RoutingRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class RoutingService {

    @Autowired
    private RoutingRuleRepository routingRuleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CohortService cohortService;

    @Autowired
    private RestTemplate restTemplate;

    // TODO — update these with actual URLs after confirming with teammates
    private static final String ADMISSION_SERVICE_URL     = "http://localhost:5000/admission/";
    private static final String BED_INVENTORY_SERVICE_URL = "http://localhost:8083/api/v1/beds/available";

    public List<RoutingRule> getAllActiveRules() {
        return routingRuleRepository.findByStatusOrderByPriorityDesc(RuleType.STATUS_ACTIVE);
    }

    // integration entry point
    // Step 1 — calls Laaranie's API to get patient details
    // Step 2 — calls Hari's API to get available beds
    // Step 3 — combines both and runs routing logic
    // Step 4 — returns safe beds to Bed Assignment module
    public Map<String, Object> routePatient(Long admissionRequestId) {

        // Step 1 — get patient details from Laaranie's admission module
        RoutingRequestDTO patientDetails;
        try {
            patientDetails = restTemplate.getForObject(
                    ADMISSION_SERVICE_URL + admissionRequestId + "/routing-payload",
                    RoutingRequestDTO.class);

            if (patientDetails == null) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("status", "ERROR");
                error.put("message", "No patient data received from Admission Service for ID: " + admissionRequestId);
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Could not reach Admission Service: " + e.getMessage());
            return error;
        }

        // Step 2 — get available beds from Hari's bed inventory module
        List<BedCandidateDTO> availableBeds;
        try {
            ResponseEntity<List<BedCandidateDTO>> response = restTemplate.exchange(
                    BED_INVENTORY_SERVICE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BedCandidateDTO>>() {}
            );
            availableBeds = response.getBody();

            if (availableBeds == null || availableBeds.isEmpty()) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("status", "ERROR");
                error.put("message", "No available beds received from Bed Inventory Service");
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Could not reach Bed Inventory Service: " + e.getMessage());
            return error;
        }

        // Step 3 — combine patient details + available beds into one request
        patientDetails.setAvailableBeds(availableBeds);

        // Step 4 — run routing logic and return result
        return findSafeBeds(patientDetails);
    }

    public BedMatchResponse validateBedAssignment(
            List<RoutingRule> activeRules,
            Long bedId,
            String isolationFlag,
            String deviceFlag,
            String lamaFlag,
            Integer patientAge,
            List<String> bedAttributes,
            String patientGender,
            String roomPolicy,
            String requestedSpeciality,
            String bedUnitType) {

        List<String> bedAttrs = bedAttributes == null ? new ArrayList<>()
                : bedAttributes.stream().map(String::toLowerCase).toList();

        for (RoutingRule rule : activeRules) {
            try {
                JsonNode json = objectMapper.readTree(rule.getExpressionJson());
                String type   = rule.getRuleType();
                int priority  = rule.getPriority();

                if (RuleType.DEVICE.equalsIgnoreCase(type)) {
                    String ruleFlag = json.get("flag").asText();
                    String required = json.get("required").asText().toLowerCase();
                    if (ruleFlag.equalsIgnoreCase(deviceFlag) && !bedAttrs.contains(required)) {
                        return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                "Device requirement not met: bed is missing [" + required + "]", type, priority);
                    }
                }

                if (RuleType.ISOLATION.equalsIgnoreCase(type)) {
                    String ruleFlag = json.get("flag").asText();
                    String required = json.get("required").asText().toLowerCase();
                    if (ruleFlag.equalsIgnoreCase(isolationFlag) && !bedAttrs.contains(required)) {
                        return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                "Isolation requirement not met: bed is missing [" + required + "]", type, priority);
                    }
                }

                if (RuleType.SPECIALTY.equalsIgnoreCase(type) && requestedSpeciality != null) {
                    String requiredUnit = json.get("unit").asText();
                    if (requestedSpeciality.equalsIgnoreCase(requiredUnit)
                            && !bedUnitType.equalsIgnoreCase(requiredUnit)) {
                        return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                "Specialty mismatch: patient needs [" + requiredUnit
                                        + "] but bed is in [" + bedUnitType + "]", type, priority);
                    }
                }

                if (RuleType.AGE.equalsIgnoreCase(type) && patientAge != null) {
                    String requiredUnit = json.get("requiredUnit").asText();
                    int minAge = json.has("minAge") ? json.get("minAge").asInt() : 0;
                    int maxAge = json.has("maxAge") ? json.get("maxAge").asInt() : Integer.MAX_VALUE;

                    if (patientAge >= minAge && patientAge <= maxAge) {
                        if ("Adult".equalsIgnoreCase(requiredUnit) && bedUnitType.equalsIgnoreCase("Pediatric")) {
                            return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                    "Age conflict: adult patient (age " + patientAge + ") cannot be in Pediatric unit",
                                    type, priority);
                        }
                        if (!"Adult".equalsIgnoreCase(requiredUnit) && !bedUnitType.equalsIgnoreCase(requiredUnit)) {
                            return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                    "Age conflict: patient aged " + patientAge + " must be in ["
                                            + requiredUnit + "] but bed is in [" + bedUnitType + "]",
                                    type, priority);
                        }
                    }
                }

                if (RuleType.LAMA.equalsIgnoreCase(type) && "LAMA".equalsIgnoreCase(lamaFlag)) {
                    List<String> allowedUnits = new ArrayList<>();
                    json.get("allowedUnits").forEach(u -> allowedUnits.add(u.asText().toLowerCase()));
                    if (!allowedUnits.contains(bedUnitType.toLowerCase())) {
                        return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                "LAMA restriction: [" + bedUnitType + "] is not allowed. Allowed: " + allowedUnits,
                                type, priority);
                    }
                }

                if (RuleType.GENDER.equalsIgnoreCase(type)) {
                    String policy = json.get("policy").asText();
                    if ("SameGenderOnly".equalsIgnoreCase(policy)
                            && !"Any".equalsIgnoreCase(roomPolicy)
                            && !roomPolicy.equalsIgnoreCase(patientGender)) {
                        return new BedMatchResponse(bedId, RuleType.STATUS_REJECTED,
                                "Gender conflict: room policy is [" + roomPolicy
                                        + "] but patient is [" + patientGender + "]", type, priority);
                    }
                }

            } catch (Exception e) {
                return new BedMatchResponse(bedId, RuleType.STATUS_ERROR,
                        "Could not read rule — check expressionJson for: " + rule.getRuleType(), "System", 0);
            }
        }

        return new BedMatchResponse(bedId, RuleType.STATUS_MATCHED,
                "Bed is clinically safe for this patient.", "None", 0);
    }

    public Map<String, Object> findSafeBeds(RoutingRequestDTO request) {

        List<RoutingRule> activeRules = getAllActiveRules();

        List<BedMatchResponse> allResults = request.getAvailableBeds().stream()
                .map(bed -> validateBedAssignment(
                        activeRules,
                        bed.getBedId(),
                        request.getIsolationFlag(),
                        request.getDeviceFlag(),
                        request.getLamaFlag(),
                        request.getPatientAge(),
                        bed.getAttributes(),
                        request.getPatientGender(),
                        bed.getCurrentRoomPolicy(),
                        request.getRequestedSpeciality(),
                        bed.getUnitType()
                )).toList();

        List<BedMatchResponse> matchedBeds  = allResults.stream()
                .filter(r -> RuleType.STATUS_MATCHED.equals(r.getStatus())).toList();
        List<BedMatchResponse> rejectedBeds = allResults.stream()
                .filter(r -> RuleType.STATUS_REJECTED.equals(r.getStatus())).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status",         "Process Completed");
        response.put("safeBedsCount",  matchedBeds.size());
        response.put("wardGroupings",  cohortService.getClinicalGrouping(matchedBeds, request.getAvailableBeds()));
        response.put("safeBeds",       matchedBeds);
        response.put("rejectionAudit", rejectedBeds);
        return response;
    }
}