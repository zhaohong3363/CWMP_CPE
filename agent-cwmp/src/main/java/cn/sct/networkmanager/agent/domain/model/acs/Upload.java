package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class Upload {
    @XmlElement(name = "CommandKey")
    private String commandKey;

    @XmlElement(name = "FileType",required = true)
    private String fileType;

    @XmlElement(name = "URL",required = true)
    private String url;

    @XmlElement(name = "Username")
    private String username;

    @XmlElement(name = "Password")
    private String password;

    @XmlElement(name = "DelaySeconds")
    private Integer delaySeconds;

}
