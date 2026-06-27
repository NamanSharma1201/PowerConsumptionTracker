package com.ncorp.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.ncorp.kafka.event.AlertingEvent;
import com.ncorp.kafka.event.EnergyUsageEvent;
import com.ncorp.usage_service.client.DeviceClient;
import com.ncorp.usage_service.client.UserClient;
import com.ncorp.usage_service.dto.DeviceDto;
import com.ncorp.usage_service.dto.UsageDto;
import com.ncorp.usage_service.dto.UserDto;
import com.ncorp.usage_service.modal.DeviceEnergy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {
    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private  final UserClient userClient;

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    @Autowired
    public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, UserClient userClient,
                        KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent){
//        log.info("Received energy usage event: {}", energyUsageEvent);
        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }

    @Scheduled(cron = "*/10 * * * * *")
    public  void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
from(bucket: "%s")
  |> range(start: time(v: "%s"), stop: time(v: "%s"))
  |> filter(fn: (r) => r["_measurement"] == "energy-usage")
  |> filter(fn: (r) => r["_field"] == "energyConsumed")
  |> group(columns:["deviceId"])
  |> sum()
""", influxBucket, oneHourAgo, now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String deviceId = (String) record.getValueByKey("deviceId");
                double energyConsumed = record.getValueByKey("_value") instanceof Number ? ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                assert deviceId != null;
                deviceEnergies.add(
                        DeviceEnergy.builder().deviceId(Long.valueOf(deviceId)).energyConsumed(energyConsumed)
                                .build()
                );
            }
        }

        log.info("Aggregated energy usage: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {

                DeviceDto deviceResponse =
                        deviceClient.getDeviceById(deviceEnergy.getDeviceId());

                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("Device {} not found", deviceEnergy.getDeviceId());
                    continue;
                }

                deviceEnergy.setUserId(deviceResponse.userId());

            } catch (Exception e) {
                log.error("Failed to fetch device {}",
                        deviceEnergy.getDeviceId(), e);
            }
        }

        deviceEnergies.removeIf(de -> de.getUserId() == null);
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap = deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));


        log.info("User device energy usage: {}", userDeviceEnergyMap);

        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet())
                ;
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for(final Long userId : userIds){
            try{
                UserDto user = userClient.getUserById(userId);
                if(user == null || user.id() == null || !user.alerting()){
                    log.warn("User with id {} not found or alerting is disabled", userId);
                    continue;
                }
                userThresholdMap.put(user.id(), user.energyAlertingThreshold());
                userEmailMap.put(user.id(), user.email());

            }catch (Exception e){
                log.warn("failed to fetch user  with id {}", userId);
            }
        }

        log.info("User threshold usage: {}", userThresholdMap);

        final List<Long> alertingUsers = new ArrayList<>(userThresholdMap.keySet());


        for(final Long userId : alertingUsers){
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);
            final double totalConsumption = devices.stream().mapToDouble(DeviceEnergy::getEnergyConsumed).sum();


            if(totalConsumption > threshold){
                final String userEmail = userEmailMap.get(userId);
                log.info("Alerting user with id {} and email {} has exceeded the threshold {} ! Total Consumption : {} energy consumed ", userId, userEmail, threshold, totalConsumption);


                final AlertingEvent alertingEvent =  AlertingEvent.builder().userId(userId)
                        .email(userEmail)
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .message("Energy consumption threshold exceeded!")
                        .build();

                kafkaTemplate.send("energy-alerts", alertingEvent);
            }else{
                log.info("User Id {} is within the threshold {} ", userId, threshold);
            }

        }


    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting usage for user {} days", days);
        final List<DeviceDto> devices = deviceClient.getAllDevicesForUser(userId);
    }
}
