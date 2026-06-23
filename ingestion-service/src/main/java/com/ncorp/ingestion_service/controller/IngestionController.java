package com.ncorp.ingestion_service.controller;

import com.ncorp.ingestion_service.dto.EnergyUsageDto;
import com.ncorp.ingestion_service.service.IngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {
    private final IngestionService ingestionService;

    @Autowired
    public IngestionController(IngestionService ingestionService){
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyUsageDto energyUsageDto){
        ingestionService.ingestEnergyUsage(energyUsageDto);
    }
}
