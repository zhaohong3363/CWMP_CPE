package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class GetParameterValuesResponse {
    @XmlElement(name = "ParameterList", required = true)
    private ParameterList parameterList;

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "GetParameterValuesParameterList")
    public static class ParameterList {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "ParameterValueStruct", namespace = "")
        private List<ParameterValueStruct> parameters;


        public void updateArrayType() {
            if (parameters != null) {
                this.arrayType = "cwmp:ParameterValueStruct[" + parameters.size() + "]";
            }
        }

        public void setParameters(List<ParameterValueStruct> parameters) {
            this.parameters = parameters;
            updateArrayType();
        }
    }

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ParameterValueStruct {

        @XmlElement(name = "Name", required = true, namespace = "")
        private String name;

        @XmlElement(name = "Value", required = true, namespace = "")
        private TypedValue value;

        public void setValue(Object value, String type) {
            this.value = new TypedValue(value, type);
        }
    }

    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TypedValue {

        @XmlValue
        private String content;

        @XmlAttribute(name = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance")
        private String type;


        public TypedValue() {}
        public TypedValue(Object value, String type) {
            this.content = (value != null) ? value.toString() : "";
            this.type = type;
        }

    }


}
