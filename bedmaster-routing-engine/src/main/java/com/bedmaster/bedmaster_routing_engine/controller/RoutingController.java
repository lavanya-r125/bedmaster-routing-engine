package com.bedmaster.bedmaster_routing_engine.controller;

import com.bedmaster.bedmaster_routing_engine.dto.BedMatchResponse;
import com.bedmaster.bedmaster_routing_engine.dto.RoutingRequestDTO;
import com.bedmaster.bedmaster_routing_engine.entities.RoutingRule;
import com.bedmaster.bedmaster_routing_engine.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/routing")
public class RoutingController {

    @Autowired
    private RoutingService routingService;

    // GET /api/v1/routing/rules
    // returns all active routing rules from the database
    @GetMapping("/rules")
    public List<RoutingRule> getActiveRules() {
        return routingService.getAllActiveRules();
    }

    // GET /api/v1/routing/check-safety
    // validates a single bed for a single patient
    // example: /check-safety?bedId=1&isolation=Airborne&device=None&lama=None
    //          &age=30&attributes=NegativePressure&gender=Male&roomPolicy=Any
    //          &speciality=ICU&bedUnit=ICU
    @GetMapping("/check-safety")
    public BedMatchResponse checkSafety(
            @RequestParam Long bedId,
            @RequestParam String isolation,
            @RequestParam(required = false, defaultValue = "None") String device,
            @RequestParam(required = false, defaultValue = "None") String lama,
            @RequestParam(required = false) Integer age,
            @RequestParam List<String> attributes,
            @RequestParam String gender,
            @RequestParam String roomPolicy,
            @RequestParam(required = false) String speciality,
            @RequestParam String bedUnit) {

        List<RoutingRule> activeRules = routingService.getAllActiveRules();

        return routingService.validateBedAssignment(
                activeRules, bedId, isolation, device,
                lama, age, attributes, gender,
                roomPolicy, speciality, bedUnit);
    }

    // POST /api/v1/routing/find-safe-beds
    // validates multiple beds for a patient and returns safe beds + rejection audit
    @PostMapping("/find-safe-beds")
    public Map<String, Object> findSafeBeds(@RequestBody RoutingRequestDTO request) {
        return routingService.findSafeBeds(request);
    }
}