package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class GetRpcMethodResponse {
    @XmlElement(name = "MethodList", required = true)
    private MethodList methodList;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    public static class MethodList {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "string", namespace = "urn:dslforum-org:cwmp-1-0")
        private List<String> methods;
        public void updateArrayType() {
            if (methods != null) {
                this.arrayType = "xsd:string[" + methods.size() + "]";
            }
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
            updateArrayType();
        }
    }
}
