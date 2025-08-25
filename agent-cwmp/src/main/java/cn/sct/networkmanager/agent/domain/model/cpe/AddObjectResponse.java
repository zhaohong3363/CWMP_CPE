package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class AddObjectResponse {
    @XmlElement(name = "InstanceNumber", required = true)
    private int instanceNumber;
    @XmlElement(name = "Status", required = true)
    private int status;
}
