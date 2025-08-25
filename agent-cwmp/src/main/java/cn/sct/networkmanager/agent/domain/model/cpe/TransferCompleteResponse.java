package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class TransferCompleteResponse {
    @XmlElement(name = "CommandKey", required = true)
    private String commandKey;
    @XmlElement(name = "StartTime", required = true)
    private String startTime;
    @XmlElement(name = "CompleteTime", required = true)
    private String completeTime;
    @XmlElement(name = "FaultStruct",required = true)
    private FaultStruct faultStruct;
    @XmlElement(name = "EstimatedTotalSize",required = true)
    private Long estimatedTotalSize;
    @XmlElement(name = "TotalBytesSent",required = true)
    private Long totalBytesSent;
    @XmlElement(name = "TotalBytesReceived",required = true)
    private Long totalBytesReceived;
    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    @AllArgsConstructor
    public static class FaultStruct {
        @XmlElement(name = "FaultCode", required = true)
        private int faultCode;
        @XmlElement(name = "FaultString", required = true)
        private String faultString;
    }
}
