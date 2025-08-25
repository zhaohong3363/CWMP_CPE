package cn.sct.networkmanager.agent.domain.model.acs;

import cn.sct.networkmanager.agent.domain.model.Envelope;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ACSInformResponseEnvelope extends Envelope {
    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

    @Override
    public String getId() {
        return header.getId().value;
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
    public static class Header {
        @XmlElement(name = "ID", namespace = "urn:dslforum-org:cwmp-1-0")
        private ID id;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class Body {
        @XmlElement(name = "InformResponse", namespace = "urn:dslforum-org:cwmp-1-0")
        private InformResponse informResponse;
    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class InformResponse {
        @XmlElement(name = "MaxEnvelopes")
        private int maxEnvelopes;
    }
}
