package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class SetParameterValues {


    @XmlElement(name = "ParameterList", required = true)

    private ParameterList parameterList;

    @XmlElement(name = "ParameterKey", required = true)
    private String parameterKey;

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SetParameterValuesParameterList")
    public static class ParameterList {
        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "ParameterValueStruct", required = true)
        private List<ParameterValueStruct> parameters;
    }

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParameterValueStruct {
        @XmlElement(name = "Name", required = true)
        private String name;

        @XmlElement(name = "Value", required = true)
        private TypedValue value;
    }

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TypedValue {
        @XmlValue
        private String value;

        @XmlAttribute(name = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance")
        private String type;
    }
}

