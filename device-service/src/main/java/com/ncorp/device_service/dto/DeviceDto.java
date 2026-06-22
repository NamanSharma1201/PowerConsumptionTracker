package com.ncorp.device_service.dto;

import com.ncorp.device_service.modal.DeviceType;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceDto {

    private Long id;
    private String name;
    private DeviceType type;
    private String location;
    private Long userId;
}
