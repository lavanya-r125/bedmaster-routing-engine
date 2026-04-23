package com.bedmaster.bedmaster_routing_engine.config;

import com.bedmaster.bedmaster_routing_engine.constants.RuleType;
import com.bedmaster.bedmaster_routing_engine.entities.CohortGroup;
import com.bedmaster.bedmaster_routing_engine.entities.RoutingRule;
import com.bedmaster.bedmaster_routing_engine.repository.CohortGroupRepository;
import com.bedmaster.bedmaster_routing_engine.repository.RoutingRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoutingRuleRepository ruleRepo, CohortGroupRepository cohortRepo) {
        return args -> {

            // only seed if tables are empty
            if (ruleRepo.count() == 0) {

                // Device rules — checked first (priority 11)
                saveRule(ruleRepo, RuleType.DEVICE, "{\"flag\": \"Bariatric\", \"required\": \"bariatricbed\"}", 11);
                saveRule(ruleRepo, RuleType.DEVICE, "{\"flag\": \"Telemetry\", \"required\": \"telemetrybed\"}", 11);
                saveRule(ruleRepo, RuleType.DEVICE, "{\"flag\": \"Crib\",      \"required\": \"pediatriccrib\"}", 11);

                // Isolation rules (priority 10 to 7)
                saveRule(ruleRepo, RuleType.ISOLATION, "{\"flag\": \"Airborne\", \"required\": \"negativepressure\"}", 10);
                saveRule(ruleRepo, RuleType.ISOLATION, "{\"flag\": \"Droplet\",  \"required\": \"singleroom\"}", 9);
                saveRule(ruleRepo, RuleType.ISOLATION, "{\"flag\": \"Contact\",  \"required\": \"singleroom\"}", 8);
                saveRule(ruleRepo, RuleType.ISOLATION, "{\"flag\": \"None\",     \"required\": \"standard\"}", 7);

                // Specialty rules (priority 9 to 7)
                saveRule(ruleRepo, RuleType.SPECIALTY, "{\"unit\": \"ICU\"}", 9);
                saveRule(ruleRepo, RuleType.SPECIALTY, "{\"unit\": \"MedSurg\"}", 8);
                saveRule(ruleRepo, RuleType.SPECIALTY, "{\"unit\": \"Pediatric\"}", 8);
                saveRule(ruleRepo, RuleType.SPECIALTY, "{\"unit\": \"Maternity\"}", 8);
                saveRule(ruleRepo, RuleType.SPECIALTY, "{\"unit\": \"StepDown\"}", 7);

                // Age rules (priority 9 to 8)
                saveRule(ruleRepo, RuleType.AGE, "{\"maxAge\": 17, \"requiredUnit\": \"Pediatric\"}", 9);
                saveRule(ruleRepo, RuleType.AGE, "{\"minAge\": 18, \"maxAge\": 120, \"requiredUnit\": \"Adult\"}", 8);

                // LAMA rule (priority 6)
                saveRule(ruleRepo, RuleType.LAMA, "{\"flag\": \"LAMA\", \"allowedUnits\": [\"ICU\", \"StepDown\", \"MedSurg\"]}", 6);

                // Gender rule (priority 7)
                saveRule(ruleRepo, RuleType.GENDER, "{\"policy\": \"SameGenderOnly\"}", 7);

                System.out.println("--- Routing Rules Initialized ---");
            }

            if (cohortRepo.count() == 0) {

                // Device cohorts (rule IDs 1, 2, 3)
                saveCohort(cohortRepo, 1,  "Bariatric Cohort",          "Beds with bariatric equipment");
                saveCohort(cohortRepo, 2,  "Telemetry Cohort",          "Beds with cardiac monitoring");
                saveCohort(cohortRepo, 3,  "Pediatric Crib Cohort",     "Beds with pediatric cribs");

                // Isolation cohorts (rule IDs 4, 5, 6, 7)
                saveCohort(cohortRepo, 4,  "Airborne Isolation Cohort", "Negative pressure rooms");
                saveCohort(cohortRepo, 5,  "Droplet Isolation Cohort",  "Single rooms for droplet patients");
                saveCohort(cohortRepo, 6,  "Contact Isolation Cohort",  "Single rooms for contact patients");
                saveCohort(cohortRepo, 7,  "General Ward Cohort",       "Standard beds");

                // Specialty cohorts (rule IDs 8, 9, 10, 11, 12)
                saveCohort(cohortRepo, 8,  "ICU Cohort",                "Critical care beds");
                saveCohort(cohortRepo, 9,  "MedSurg Cohort",            "Medical surgical beds");
                saveCohort(cohortRepo, 10, "Pediatric Cohort",          "Beds for patients under 18");
                saveCohort(cohortRepo, 11, "Maternity Cohort",          "Labor and delivery beds");
                saveCohort(cohortRepo, 12, "StepDown Cohort",           "Step-down unit beds");

                // Age cohorts (rule IDs 13, 14)
                saveCohort(cohortRepo, 13, "Pediatric Age Cohort",      "Patients aged 0 to 17");
                saveCohort(cohortRepo, 14, "Adult Age Cohort",          "Patients aged 18 and above");

                // LAMA cohort (rule ID 15)
                saveCohort(cohortRepo, 15, "LAMA Supervised Cohort",    "LAMA patients in monitored units only");

                // Gender cohort (rule ID 16)
                saveCohort(cohortRepo, 16, "Gender-Segregated Cohort",  "Same gender rooms only");

                System.out.println("--- Cohort Groups Initialized ---");
            }
        };
    }

    // helper — creates and saves one routing rule
    private void saveRule(RoutingRuleRepository repo, String type, String json, int priority) {
        RoutingRule rule = new RoutingRule();
        rule.setRuleType(type);
        rule.setExpressionJson(json);
        rule.setPriority(priority);
        rule.setStatus(RuleType.STATUS_ACTIVE);
        repo.save(rule);
    }

    // helper — creates and saves one cohort group
    private void saveCohort(CohortGroupRepository repo, int ruleId, String label, String notes) {
        CohortGroup cohort = new CohortGroup();
        cohort.setRuleId(ruleId);
        cohort.setLabel(label);
        cohort.setNotes(notes);
        repo.save(cohort);
    }
}