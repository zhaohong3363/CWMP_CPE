package cn.sct.networkmanager.agent.domain.model.cpe;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class GetParameterNamesResponse {
    @XmlElement(name = "ParameterList", required = true)
    private ParameterList parameterList;

    public GetParameterNamesResponse() {}

    public GetParameterNamesResponse(List<ParameterInfoStruct> params) {
        this.parameterList = new ParameterList(params);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    @XmlType(name = "GetParameterNamesParameterList")
    public static class ParameterList {
        @XmlAttribute(name = "arrayType", namespace = "http://schemas.xmlsoap.org/soap/encoding/")
        private String arrayType;

        @XmlElement(name = "ParameterInfoStruct")
        private List<ParameterInfoStruct> items = new ArrayList<>();

        public ParameterList() {}

        public ParameterList(List<ParameterInfoStruct> params) {
            this.items = params;
            this.arrayType = "cwmp:ParameterInfoStruct[" + params.size() + "]";
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Getter
    @Setter
    public static class ParameterInfoStruct {
        @XmlElement(name = "Name", required = true)
        private String name;

        @XmlElement(name = "Writable", required = true)
        private int writable;

        // 构造方法
        public ParameterInfoStruct() {}

        public ParameterInfoStruct(String name, int writable) {
            this.name = name;
            this.writable = writable ;
        }
    }

}
