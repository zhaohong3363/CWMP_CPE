
package cn.sct.networkmanager.agent.domain.model;

import cn.sct.networkmanager.agent.domain.entity.DeviceInfo;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class FalutResponseEnvelope extends Envelope {

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
        @XmlElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
        private CwmpFault fault;
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
        private FalutResponseEnvelope.ID id;
    }

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CwmpFault {

        @XmlElement(name = "faultcode", namespace = "")
        private String faultcode = "Client";

        @XmlElement(name = "faultstring", namespace = "")
        private String faultstring = "CWMP fault";

        @XmlElement(name = "detail", namespace = "")
        private FaultDetail detail;

        public CwmpFault() {}

        public CwmpFault(int faultCode, String faultMessage) {
            this.detail = new FaultDetail(faultCode, faultMessage);
        }

        public CwmpFault(int faultCode, String faultMessage, String paramName, int paramFaultCode, String paramFaultMessage) {
            this.detail = new FaultDetail(faultCode, faultMessage, paramName, paramFaultCode, paramFaultMessage);
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @Getter
        @Setter
        public static class FaultDetail {

            @XmlElement(name = "Fault", namespace = "urn:dslforum-org:cwmp-1-0")
            private CwmpFaultStruct fault;

            public FaultDetail(int faultCode, String faultMessage) {
                this.fault = new CwmpFaultStruct(faultCode, faultMessage);
            }

            public FaultDetail(int faultCode, String faultMessage, String paramName, int paramFaultCode, String paramFaultMessage) {
                this.fault = new CwmpFaultStruct(faultCode, faultMessage);
                this.fault.addSetParameterValuesFault(paramName, paramFaultCode, paramFaultMessage);
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @Getter
        @Setter
        public static class CwmpFaultStruct {

            @XmlElement(name = "FaultCode", namespace = "")
            private int faultCode;

            @XmlElement(name = "FaultString", namespace = "")
            private String faultString;

            @XmlElement(name = "SetParameterValuesFault", namespace = "urn:dslforum-org:cwmp-1-0")
            private SetParameterValuesFault setParameterValuesFault;

            public CwmpFaultStruct() {}

            public CwmpFaultStruct(int faultCode, String faultString) {
                this.faultCode = faultCode;
                this.faultString = faultString;
            }

            public void addSetParameterValuesFault(String paramName, int paramFaultCode, String paramFaultString) {
                if (this.setParameterValuesFault == null) {
                    this.setParameterValuesFault = new SetParameterValuesFault();
                }
                this.setParameterValuesFault.addFault(paramName, paramFaultCode, paramFaultString);
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @Getter
        @Setter
        public static class SetParameterValuesFault {
            @XmlElement(name = "ParameterName", namespace = "urn:dslforum-org:cwmp-1-0")
            private String parameterName;
            @XmlElement(name = "FaultCode", namespace = "urn:dslforum-org:cwmp-1-0")
            private int faultCode;
            @XmlElement(name = "FaultString", namespace = "urn:dslforum-org:cwmp-1-0")
            private String faultString;
            @XmlElement(name = "SetParameterValuesFault", namespace = "urn:dslforum-org:cwmp-1-0")
            private SetParameterValuesFault nextFault;
            public void addFault(String paramName, int faultCode, String faultString) {
                if (this.parameterName == null) {
                    this.parameterName = paramName;
                    this.faultCode = faultCode;
                    this.faultString = faultString;
                } else {
                    if (this.nextFault == null) {
                        this.nextFault = new SetParameterValuesFault();
                    }
                    this.nextFault.addFault(paramName, faultCode, faultString);
                }
            }
        }
    }
}
