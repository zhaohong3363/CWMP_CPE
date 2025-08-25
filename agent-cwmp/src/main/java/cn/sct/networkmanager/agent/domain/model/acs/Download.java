package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class Download {

    @XmlElement(name = "CommandKey", required = true)
    private String commandKey;

    @XmlElement(name = "FileType", required = true)
    private String fileType;

    @XmlElement(name = "URL", required = true)
    private String url;

    @XmlElement(name = "Username")
    private String username;

    @XmlElement(name = "Password")
    private String password;

    @XmlElement(name = "FileSize")
    private Long fileSize;

    @XmlElement(name = "TargetFileName")
    private String targetFileName;

    @XmlElement(name = "DelaySeconds")
    private Integer delaySeconds;

    @XmlElement(name = "SuccessURL")
    private String successURL;

    @XmlElement(name = "FailureURL")
    private String failureURL;

}
