package com.ncorp.insight_service.service;

import com.ncorp.insight_service.client.UsageClient;
import com.ncorp.insight_service.dto.DeviceDto;
import com.ncorp.insight_service.dto.InsightDto;
import com.ncorp.insight_service.dto.UsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InsightService {
    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;
    public InsightService(UsageClient usageClient,
                          OllamaChatModel ollamaChatModel) {
        this.usageClient = usageClient;
        this.ollamaChatModel = ollamaChatModel;
    }

    public InsightDto getSavingTips(Long userId) {
        final UsageDto usageDto = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageDto.devices().stream().mapToDouble(DeviceDto::energyConsumed).sum();

        log.info("Calling Ollama for userId {} with totalUsage {}",  userId, totalUsage);

        String prompt = "This is my total consumption over the past 3 days." +
                "How can I reduce my energy consumption? How does it compare to average households?" +
                "Total energy used: \n" +
                totalUsage;

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();

    }

    public InsightDto getOverview(Long userId) {
        final UsageDto usageDto = usageClient.getXDaysUsageForUser(userId, 3);
        log.info(usageDto.toString());
        double totalUsage = usageDto.devices().stream().mapToDouble(DeviceDto::energyConsumed).sum();

        log.info("Calling Ollama for userId {} with totalUsage {}",  userId, totalUsage);

        String prompt = "Analyse the following energy usage data and provide a " +
                "concise overview with actionable insights." +
                "This data is the aggregate data for the past 3 days." +
                "Usage Data: \n" +
                usageDto.devices();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }

}
