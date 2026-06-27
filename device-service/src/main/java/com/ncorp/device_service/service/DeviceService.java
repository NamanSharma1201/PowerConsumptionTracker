package com.ncorp.device_service.service;

import com.ncorp.device_service.dto.DeviceDto;
import com.ncorp.device_service.entity.Device;
import com.ncorp.device_service.exception.DeviceNotFoundException;
import com.ncorp.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository){
        this.deviceRepository = deviceRepository;
    }

    public  DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setName(deviceDto.getName());
        device.setType(deviceDto.getType());
        device.setLocation(deviceDto.getLocation());
        device.setUserId(deviceDto.getUserId());
        final Device savedDevice = deviceRepository.save(device);

        return mapToDto(savedDevice);
    }


    public DeviceDto getDeviceById(Long id){
        Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Device not found with id " + id));

        return mapToDto(device);
    }


    private DeviceDto mapToDto(Device device){
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }


    public DeviceDto updateDevice(Long id, DeviceDto input) {

            Device device = deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("device not found with id: " + id));

            device.setName(input.getName());
            device.setType(input.getType());
            device.setLocation(input.getLocation());
            device.setUserId(input.getUserId());
            return mapToDto(deviceRepository.save(device));

    }

    public void deleteDevice(long id) {
        if(!deviceRepository.existsById(id)){
            throw new DeviceNotFoundException("Device not found");
        }
        deviceRepository.deleteById(id);
    }

    public List<DeviceDto> getAllDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findAllByUserId(userId);
        log.info(devices.toString());
        return devices.stream()
                .map(this::mapToDto)
                .toList();
    }
}
