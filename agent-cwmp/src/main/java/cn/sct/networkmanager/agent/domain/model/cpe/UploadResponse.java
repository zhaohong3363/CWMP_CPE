package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class UploadResponse {
    @XmlElement(name = "Status", required = true)
    private int status;

    @XmlElement(name = "StartTime", required = true)
    private String startTime;

    @XmlElement(name = "CompleteTime", required = true)
    private String completeTime;

    public UploadResponse(Integer status) {
        this.status=status;
    }
}
