package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class SetParameterValuesResponse {
    @XmlElement(name = "Status", required = true)
    private int status;

    public SetParameterValuesResponse(int status) {
        this.status = status;
    }

}
