package cn.sct.networkmanager.agent.domain.model.cpe;

import cn.sct.networkmanager.agent.domain.model.Envelope;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CPEMethodResponseEnvelope extends Envelope {

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
                @XmlElement(name = "GetParameterValuesResponse", type = GetParameterValuesResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "GetRPCMethodsResponse", type = GetRpcMethodResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "SetParameterValuesResponse", type = SetParameterValuesResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "GetParameterNamesResponse", type = GetParameterNamesResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "RebootResponse", type = RebootResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "FactoryResetResponse", type = FactoryResetResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "UploadResponse", type = UploadResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "DownloadResponse", type = DownloadResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "TransferCompleteResponse", type = TransferCompleteResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "AddObjectResponse", type = AddObjectResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "DeleteObjectResponse", type = DeleteObjectResponse.class, namespace = "urn:dslforum-org:cwmp-1-0"),
                @XmlElement(name = "SetParameterAttributesResponse", type = SetParameterAttributesResponse.class, namespace = "urn:dslforum-org:cwmp-1-0")

        })
        private Object methodResponse;
    }
    @Override
    public String getId() {
        return header.id.value;
    }
}
