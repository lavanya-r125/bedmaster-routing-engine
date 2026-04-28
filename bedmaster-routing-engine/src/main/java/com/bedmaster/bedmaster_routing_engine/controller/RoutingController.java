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
    @GetMapping("/rules")
    public List<RoutingRule> getActiveRules() {
        return routingService.getAllActiveRules();
    }

    // GET /api/v1/routing/check-safety
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
    // used for manual testing — accepts full JSON body
    @PostMapping("/find-safe-beds")
    public Map<String, Object> findSafeBeds(@RequestBody RoutingRequestDTO request) {
        return routingService.findSafeBeds(request);
    }

    // POST /api/v1/routing/route-patient/{admissionRequestId}
    // integration endpoint — calls Laaranie + Hari automatically
    // TODO — test after confirming URLs with teammates
    @PostMapping("/route-patient/{admissionRequestId}")
    public Map<String, Object> routePatient(@PathVariable Long admissionRequestId) {
        return routingService.routePatient(admissionRequestId);
    }
}