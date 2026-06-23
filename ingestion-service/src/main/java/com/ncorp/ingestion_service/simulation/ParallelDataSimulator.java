package com.ncorp.ingestion_service.simulation;

import com.ncorp.ingestion_service.dto.EnergyUsageDto;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ParallelDataSimulator implements CommandLineRunner {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.endpoint}")
    private String ingestionEndpoint;

    @Value("${simulation.parallel-thread}")
    private int parallelThreads;

    @Value("${simulation.requests-per-interval}")
    private int requestPerInterval;

    private final ExecutorService executorService;

    public ParallelDataSimulator( ){
        this.executorService = Executors.newCachedThreadPool();
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);
    }

    @Override
    public void run(String... args) throws Exception{
        ((ThreadPoolExecutor)executorService).setCorePoolSize(parallelThreads);

        log.info("Parallel Data Simulator started....");
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData(){
        int batchSize = requestPerInterval / parallelThreads;
        int remainder = requestPerInterval % parallelThreads;

        for(int i = 0; i < parallelThreads; i++){
            int requestForThread = batchSize + (i < remainder ? 1 : 0);
            executorService.submit(() ->{
                for(int j = 0; j < requestForThread; j++){
                    EnergyUsageDto dto = EnergyUsageDto.builder()
                            .deviceId(random.nextLong(1, 6))
                            .energyConsumed(Math.round(random.nextDouble(0.0, 2.0) * 100.0))
                            .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()).build();

                    try{
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<EnergyUsageDto> request = new HttpEntity<>(dto, headers);
                        restTemplate.postForEntity(ingestionEndpoint, request, Value.class);
                        log.info("Send mock data: {}", dto);
                    } catch (Exception e) {
                        log.error("Failed to send the data: {}", e.getMessage());
                    }
                }
            });
        }
    }

    @PreDestroy
    public void shutDown(){
        executorService.shutdown();
        log.info("Parallel Data Simulator shut down ....");
    }
}

