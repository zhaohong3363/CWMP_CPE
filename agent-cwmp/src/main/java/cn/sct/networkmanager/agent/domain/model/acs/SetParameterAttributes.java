package cn.sct.networkmanager.agent.domain.model.acs;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class SetParameterAttributes {
    @XmlElement(name = "ParameterList", required = true)
    private ParameterList parameterList;


    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    @XmlType(name = "SetParameterAttributesParameterList")
    public  static class ParameterList {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "SetParameterAttributesStruct")
        private List<SetParameterAttributesStruct> setParameterAttributesStructs;


    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public  static class SetParameterAttributesStruct {

        @XmlElement(name = "Name")
        private String name;

        @XmlElement(name = "NotificationChange")
        private Integer notificationChange;

        @XmlElement(name = "Notification")
        private Integer notification;

        @XmlElement(name = "AccessListChange")
        private Integer accessListChange;

        @XmlElement(name = "AccessList")
        private AccessList accessList;


    }
    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public   static  class AccessList {

        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "string")
        private List<StringValue> strings;


    }
    @XmlAccessorType(XmlAccessType.FIELD)
   public static  class StringValue {

        @XmlAttribute(name = "type", namespace = "http://www.w3.org/2001/XMLSchema-instance")
        private String type;

        @XmlValue
        private String value;

    }

}
