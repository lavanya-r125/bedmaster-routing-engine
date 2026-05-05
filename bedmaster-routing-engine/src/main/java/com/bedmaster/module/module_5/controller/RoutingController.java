package com.bedmaster.module.module_5.controller;

import com.bedmaster.module.module_5.dto.BedMatchResponse;
import com.bedmaster.module.module_5.dto.RoutingRequestDTO;
import com.bedmaster.module.module_5.entities.RoutingRule;
import com.bedmaster.module.module_5.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/routing")
public class RoutingController {

    @Autowired
    private RoutingService routingService;

    @GetMapping("/rules")
    public List<RoutingRule> getActiveRules() {
        return routingService.getAllActiveRules();
    }

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

    @PostMapping("/find-safe-beds")
    public Map<String, Object> findSafeBeds(@RequestBody RoutingRequestDTO request) {
        return routingService.findSafeBeds(request);
    }
}