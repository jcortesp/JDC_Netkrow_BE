// src/main/java/com/netkrow/backend/controller/HealthController.java
package com.netkrow.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of("status", "UP");
    }
}
