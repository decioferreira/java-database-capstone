package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        return isValid(token, "admin") ? "admin/adminDashboard" : "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        return isValid(token, "doctor") ? "doctor/doctorDashboard" : "redirect:/";
    }

    // Service.validateToken returns an empty body when the token is valid, or an error message otherwise
    private boolean isValid(String token, String role) {
        ResponseEntity<Map<String, String>> result = service.validateToken(token, role);
        Map<String, String> errors = result.getBody();
        return result.getStatusCode().is2xxSuccessful() && (errors == null || errors.isEmpty());
    }
}
