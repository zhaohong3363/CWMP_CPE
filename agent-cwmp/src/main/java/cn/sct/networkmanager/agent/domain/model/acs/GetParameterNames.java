package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class GetParameterNames {
    @XmlElement(name = "ParameterPath", required = true)
    private String parameterPath;

    @XmlElement(name = "NextLevel", required = true)
    private Integer nextLevel;

}
