package cn.sct.networkmanager.agent.domain.model.acs;

import cn.sct.networkmanager.agent.domain.model.Envelope;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class AcsMethodRequestEnvelope extends Envelope {
    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

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
    public static class Header {
        @XmlElement(name = "ID", namespace = "urn:dslforum-org:cwmp-1-0")
        private ID id;
        @XmlElement(name = "NoMoreRequests")
        private Integer noMoreRequests;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Body {
        @XmlElements({
                @XmlElement(name = "GetParameterValues", type = GetParameterValues.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "SetParameterValues", type = SetParameterValues.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "GetParameterNames", type = GetParameterNames.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "GetRPCMethods", type = GetRPCMethods.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "Download", type = Download.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "Reboot", type = Reboot.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "FactoryReset", type = FactoryReset.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "Upload", type = Upload.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "AddObject", type = AddObject.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "DeleteObject", type = DeleteObject.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "SetParameterAttributes", type = SetParameterAttributes.class, namespace = "urn:dslforum-org:cwmp-1-0")
        })
        private Object methodRequest;
    }
    @Override
    public String getId() {
        return header.id.value;
    }
}
