package cn.sct.networkmanager.agent.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DeviceInfo {
    private String manufacturer;
    private String oui;
    private String productClass;
    private String serialNumber;
    private String hardwareVersion;
    private Map<String, String> parameter=new HashMap<>();




}
