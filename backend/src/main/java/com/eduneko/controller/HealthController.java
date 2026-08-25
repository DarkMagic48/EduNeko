package com.eduneko.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {

        return Map.of(
            "status", "UP",
            "application", "EduNeko Backend"
        );
    }
}
