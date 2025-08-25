
package cn.sct.networkmanager.agent.domain.model.cpe;

import cn.sct.networkmanager.agent.domain.entity.DeviceInfo;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CPEInformRequestEnvelope extends Envelope {

    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

    @Override
    public String getId() {
        return header.getId().getValue();
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Body {
        @XmlElement(name = "Inform", namespace = "urn:dslforum-org:cwmp-1-0")
        private Inform inform;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class DeviceId {
        @XmlElement(name="Manufacturer")
        private String manufacturer;
        @XmlElement(name="OUI")
        private String oui;
        @XmlElement(name="ProductClass")
        private String productClass;
        @XmlElement(name="SerialNumber")
        private String serialNumber;
        @XmlElement(name="HardwareVersion")
        private String hardwareVersion;
        public DeviceId(){};
        public DeviceId(DeviceInfo deviceInfo){
            this.manufacturer = deviceInfo.getManufacturer();
            this.oui = deviceInfo.getOui();
            this.productClass = deviceInfo.getProductClass();
            this.serialNumber = deviceInfo.getSerialNumber();
            this.hardwareVersion = deviceInfo.getHardwareVersion();
        }
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Event {
        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType = "cwmp:EventStruct[1]";

        @XmlElement(name = "EventStruct")
        private List<EventStruct> eventStruct;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class EventStruct {
        @XmlElement(name = "EventCode")
        private String eventCode;
        @XmlElement(name = "CommandKey")
        private String commandKey;

        public EventStruct(String eventCode, String commandKey){
            this.eventCode = eventCode;
            this.commandKey = commandKey;
        }
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class ID {
        @XmlAttribute(name = "mustUnderstand", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
        private int mustUnderstand = 1;
        @XmlValue
        private String value;

    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Inform {
        @XmlElement(name = "DeviceId")
        private DeviceId deviceId;

        @XmlElement(name = "Event")
        private Event event;
        @XmlElement(name = "MaxEnvelopes")
        private int maxEnvelopes;
        @XmlElement(name = "CurrentTime")
        private String currentTime;
        @XmlElement(name = "RetryCount")
        private int retryCount;
        @XmlElement(name = "ParameterList")
        private ParameterList parameterList;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class ParameterList {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType = "cwmp:ParameterValueStruct[8]";

        @XmlElement(name = "ParameterValueStruct")
        private List<ParameterValueStruct> parameterValueStruct;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class ParameterValueStruct {
        @XmlElement(name = "Name")
        private String name;

        @XmlElement(name = "Value")
        private Value value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Value {
        @XmlAttribute(name = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance")
        private String type = "xsd:string";

        @XmlValue
        private String content;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Header {
        @XmlElement(name = "ID", namespace = "urn:dslforum-org:cwmp-1-0")
        private CPEInformRequestEnvelope.ID id;
    }

}
