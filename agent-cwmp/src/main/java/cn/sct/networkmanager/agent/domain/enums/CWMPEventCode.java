package cn.sct.networkmanager.agent.domain.enums;

public enum CWMPEventCode {
    CONNECTION_REQUEST,//acs主动请求连接
    BOOTSTRAP,
    BOOT,
    PERIODIC,
    TRANSFER_COMPLETE,
    MReboot,//acs发起的设备重启
    MDownload,//acs要求cpe下载文件，触发重启
    VALUE_CHANGE;

}
