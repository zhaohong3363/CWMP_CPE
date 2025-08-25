package cn.sct.networkmanager.agent.config;


import cn.sct.networkmanager.agent.domain.entity.DeviceInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@ConfigurationProperties(prefix = "cwmp.agent")
public class CPEConfigProperties {
    private boolean enable = false;
    private String acsHost;//acs地址
    private int acsPort;//acs端口
    private String requestUrl = "";//请求acs地址
    private String receiveUrl = "";//接收请求地址
    private String host;//主动监听的地址
    private int port;//主动监听端口
    private int connectionLimit;//美分钟最大请求数
    private boolean useSSL = false;
    private boolean useClientAuth = false;
    private File clientCertFile = null;      // 客户端证书文件（用于keyManager）
    private File clientKeyFile = null;       // 客户端私钥文件（用于keyManager）
    private File trustCertFile = null;       // 信任证书文件（用于trustManager）
    private Integer connectTimeoutSeconds = 10;
    private int maxRetryAttempts = Integer.MAX_VALUE;
    private int retrySendMessage = 3;
    private long retrySendMessageDelayMillis = 5000;
    private long maxDelayMillis = 5 * 60 * 1000;
    private long requestTimeoutSeconds = 30;
    private DeviceInfo deviceInfo;

    private int qps = 100;

    private boolean isBootstrap = false;

    private String userName;
    private String password;

}
