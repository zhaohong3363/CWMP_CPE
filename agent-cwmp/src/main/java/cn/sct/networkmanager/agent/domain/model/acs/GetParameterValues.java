package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class GetParameterValues {

    @XmlElement(name = "ParameterNames", required = true)
    private ParameterNames parameterNames;

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParameterNames {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "string", required = true)
        private List<String> names;

    }
}
