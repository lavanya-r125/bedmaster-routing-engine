package com.bedmaster.module.module_5.service;

import com.bedmaster.module.module_5.dto.BedCandidateDTO;
import com.bedmaster.module.module_5.dto.BedMatchResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CohortService {

    public Map<String, Object> getClinicalGrouping(
            List<BedMatchResponse> matchedBeds,
            List<BedCandidateDTO> allBeds) {

        Set<Long> matchedIds = matchedBeds.stream()
                .map(BedMatchResponse::getBedId)
                .collect(Collectors.toSet());

        List<Long> isolationCohort = new ArrayList<>();
        List<Long> icuCohort       = new ArrayList<>();
        List<Long> pedsCohort      = new ArrayList<>();
        List<Long> maternityCohort = new ArrayList<>();
        List<Long> generalCohort   = new ArrayList<>();

        for (BedCandidateDTO bed : allBeds) {
            if (!matchedIds.contains(bed.getBedId())) continue;

            List<String> attrs = bed.getAttributes() == null ? new ArrayList<>()
                    : bed.getAttributes().stream().map(String::toLowerCase).toList();

            String unit = bed.getUnitType() == null ? "" : bed.getUnitType().toLowerCase();

            if (attrs.contains("negativepressure") || attrs.contains("singleroom"))
                isolationCohort.add(bed.getBedId());

            if (unit.contains("icu"))
                icuCohort.add(bed.getBedId());
            else if (unit.contains("pediatric") || unit.contains("child"))
                pedsCohort.add(bed.getBedId());
            else if (unit.contains("maternity") || unit.contains("labor") || unit.contains("ob"))
                maternityCohort.add(bed.getBedId());
            else
                generalCohort.add(bed.getBedId());
        }

        Map<String, Object> groupings = new LinkedHashMap<>();
        groupings.put("Isolation Cohort", isolationCohort);
        groupings.put("ICU / Step-down",  icuCohort);
        groupings.put("Pediatrics",        pedsCohort);
        groupings.put("Maternity",         maternityCohort);
        groupings.put("General Ward",      generalCohort);
        return groupings;
    }
}