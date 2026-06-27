package com.ncorp.insight_service.service;

import com.ncorp.insight_service.client.UsageClient;
import com.ncorp.insight_service.dto.InsightDto;
import com.ncorp.insight_service.dto.UsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InsightService {
    private final UsageClient usageClient;

    public InsightService(UsageClient usageClient){
        this.usageClient = usageClient;
    }

    public InsightDto getSavingTips(Long userId) {
    }

    public InsightDto getOverview(Long userId) {
        final UsageDto usageDto = usageClient.getXDaysUsageForUser(userId, 3);
    }
}
