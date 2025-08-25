package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class AddObject {
    @XmlElement(name = "ObjectName", required = true)
    private String objectName;
    @XmlElement(name = "ParameterKey", required = true)
    private String parameterKey;
}
