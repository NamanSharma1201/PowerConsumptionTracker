package com.ncorp.usage_service.modal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceEnergy{
    Long deviceId;
    double energyConsumed;
    Long userId;
}
