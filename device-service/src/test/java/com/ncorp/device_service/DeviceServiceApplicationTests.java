package com.ncorp.device_service;

import com.ncorp.device_service.entity.Device;
import com.ncorp.device_service.modal.DeviceType;
import com.ncorp.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {
    @Autowired
    private DeviceRepository deviceRepository;

	@Test
	void contextLoads() {
	}
    @Disabled
    @Test
    void createDevices(){

        for(int i = 0; i < 30; i++){
            Device device = Device.builder().
                    name("Device" + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("Test Location " + i)
                    .userId((long) ((i % 10) + 1)).build();
            deviceRepository.save(device);
        }
        log.info("Device Repository has been populated");

    }

}
