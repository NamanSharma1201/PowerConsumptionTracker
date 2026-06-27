package com.ncorp.usage_service.controller;

import com.ncorp.usage_service.dto.DeviceDto;
import com.ncorp.usage_service.dto.UsageDto;
import com.ncorp.usage_service.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {
    private final UsageService usageService;

    @Autowired
    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDto> getUserDeviceUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int days) {

        final UsageDto usage = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usage);
    }
}
