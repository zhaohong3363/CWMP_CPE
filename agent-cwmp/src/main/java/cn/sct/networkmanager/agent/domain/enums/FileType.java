package cn.sct.networkmanager.agent.domain.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FileType {

    /**
     * 固件升级镜像
     */
    FIRMWARE_UPGRADE_IMAGE(1, "Firmware Upgrade Image"),

    /**
     * 厂商日志文件
     */
    VENDOR_LOG_FILE(2, "Vendor Log File"),

    /**
     * 厂商配置文件
     */
    VENDOR_CONFIGURATION_FILE(3, "Vendor Configuration File"),

    /**
     * 厂商设备证书
     */
    VENDOR_DEVICE_CERTIFICATE(4, "Vendor Device Certificate"),

    /**
     * 厂商客户证书
     */
    VENDOR_CUSTOMER_CERTIFICATE(5, "Vendor Customer Certificate"),

    /**
     * 厂商SP证书
     */
    VENDOR_SP_CERTIFICATE(6, "Vendor SP Certificate"),

    /**
     * 厂商CPE证书
     */
    VENDOR_CPE_CERTIFICATE(7, "Vendor CPE Certificate"),

    /**
     * 厂商ACS证书
     */
    VENDOR_ACS_CERTIFICATE(8, "Vendor ACS Certificate");

    private final int code;
    private final String description;

    FileType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static FileType fromFiletype(String des) {
        String[] s = des.split(" ");
        List<String> list = new ArrayList<>(Arrays.asList(s));
        if (list.size() > 1){
            list.remove(0);
        }
       des=String.join(" ", list);
        if (des.equals("Firmware Upgrade Image")){
            return FIRMWARE_UPGRADE_IMAGE;
        }
        else if (des.equals("Vendor Log File")){
            return VENDOR_LOG_FILE;
        }
        else if (des.equals("Vendor Configuration File")){
            return VENDOR_CONFIGURATION_FILE;
        }
        else if (des.equals("Vendor Device Certificate")){
            return VENDOR_DEVICE_CERTIFICATE;
        }
        else if (des.equals("Vendor Customer Certificate")){
            return VENDOR_CUSTOMER_CERTIFICATE;
        }
        else if (des.equals("Vendor SP Certificate")){
            return VENDOR_SP_CERTIFICATE;
        }
        else if (des.equals("Vendor CPE Certificate")){
            return VENDOR_CPE_CERTIFICATE;
        }
        else if (des.equals("Vendor ACS Certificate")){
            return VENDOR_ACS_CERTIFICATE;
        }
        throw new RuntimeException("FileType not support");
    }

}
