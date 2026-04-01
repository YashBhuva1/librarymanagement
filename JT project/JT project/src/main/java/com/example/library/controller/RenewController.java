package com.example.library.controller;

import com.example.library.service.RenewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/renewals")
public class RenewController {

    private final RenewService renewService;

    public RenewController(RenewService renewService) {
        this.renewService = renewService;
    }

    /**
     * Shows students who have exceeded the 8-day time limit.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<String>> getOverdueStudents() {
        return ResponseEntity.ok(renewService.getOverdueStudentNames());
    }

    @GetMapping("/my")
    public ResponseEntity<List<com.example.library.entity.Renew>> getMyRenewals(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(renewService.getActiveRenewalsForUser(email));
    }

    @PostMapping("/admin/renew/{borrowId}")
    public ResponseEntity<Void> manualRenew(@PathVariable Long borrowId) {
        renewService.manualRenew(borrowId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/user/{email}")
    public ResponseEntity<List<com.example.library.entity.Renew>> getRenewalsByUserEmail(@PathVariable String email) {
        return ResponseEntity.ok(renewService.getActiveRenewalsForUser(email));
    }
}
