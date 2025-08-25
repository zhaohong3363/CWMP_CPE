package cn.sct.networkmanager.agent.protocol.cwmp.soap;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.domain.CWMPElement;
import cn.sct.networkmanager.agent.domain.entity.DeviceInfo;
import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.FalutResponseEnvelope;
import cn.sct.networkmanager.agent.domain.model.cpe.*;
import cn.sct.networkmanager.agent.element.CWMPElementManager;
import cn.sct.networkmanager.agent.element.ElementTreeNode;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.DownloadHandler;
import org.springframework.util.StringUtils;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SoapCreateFactory {
    private static final AtomicInteger count = new AtomicInteger(0);
    public static Envelope createBootstrapInform(DeviceInfo deviceInfo, CWMPEventCode code, int retryCount,String commandKey) {
        CPEInformRequestEnvelope CPEInformRequestEnvelope = new CPEInformRequestEnvelope();
        // 构建Header
        CPEInformRequestEnvelope.Header header = new CPEInformRequestEnvelope.Header();
        CPEInformRequestEnvelope.ID id = new CPEInformRequestEnvelope.ID();
        id.setValue(String.valueOf(count.addAndGet(1)));
        header.setId(id);
        CPEInformRequestEnvelope.setHeader(header);
        // 构建Body
        CPEInformRequestEnvelope.Body body = new CPEInformRequestEnvelope.Body();
        CPEInformRequestEnvelope.Inform inform = new CPEInformRequestEnvelope.Inform();
        body.setInform(inform);
        CPEInformRequestEnvelope.setBody(body);
        inform.setDeviceId(new CPEInformRequestEnvelope.DeviceId(deviceInfo));
        CPEInformRequestEnvelope.Event event = new CPEInformRequestEnvelope.Event();
        List<CPEInformRequestEnvelope.EventStruct> eventStructs = new ArrayList<>();
        if (code==CWMPEventCode.BOOTSTRAP){//设备首次启动可能触发配置下发
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("0 BOOTSTRAP",""));
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("1 BOOT",""));
        } else if (code==CWMPEventCode.BOOT) {
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("1 BOOT",commandKey));
        } else if (code==CWMPEventCode.CONNECTION_REQUEST){
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("6 CONNECTION REQUEST",""));
        } else if (code == CWMPEventCode.PERIODIC) {//定期通知
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("2 PERIODIC",""));
        } else if (code==CWMPEventCode.MReboot){
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("M Reboot",commandKey));
        }else if (code==CWMPEventCode.MDownload){
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("M Download",commandKey));
        }else if (code==CWMPEventCode.VALUE_CHANGE){
            eventStructs.add( new CPEInformRequestEnvelope.EventStruct("4 VALUE CHANGE",""));
        }
        event.setArrayType("cwmp:EventStruct["+eventStructs.size()+"]");
        event.setEventStruct(eventStructs);
        inform.setEvent(event);
        inform.setMaxEnvelopes(1);
        inform.setCurrentTime(getCurrentTimeFormatted());
        inform.setRetryCount(retryCount);
        CPEInformRequestEnvelope.ParameterList paramList = new CPEInformRequestEnvelope.ParameterList();
        List<CPEInformRequestEnvelope.ParameterValueStruct> paramValueStructs = new ArrayList<>();
        for (String key:deviceInfo.getParameter().keySet()){
            paramValueStructs.add(createParam(key,deviceInfo.getParameter().get(key)));
        }
        //附加被动或主动通知参数
        List<ElementTreeNode> allElementTreeNode = CWMPElementManager.getAllElementTreeNode();
        for (ElementTreeNode node:allElementTreeNode){
            Item<Object> value = node.getValue();
            if (value instanceof CWMPElement){
                CWMPElement<Object> CWMPElement = (CWMPElement<Object>) value;
                if (((CWMPElement<Object>) value).getNotification()
                        != cn.sct.networkmanager.agent.domain.CWMPElement.Notification.NONE
                && !CWMPElement.isProcessed()){
                    paramValueStructs.add(createParam(node.getFullName()
                            ,String.valueOf(value.getValue())));
                    CWMPElement.setProcessed(true);
                }
            }
        }
        paramList.setParameterValueStruct(paramValueStructs);
        paramList.setArrayType("cwmp:ParameterValueStruct["+paramValueStructs.size()+"]");
        inform.setParameterList(paramList);
        return CPEInformRequestEnvelope;
    }

    public static Envelope createGetParameterValuesResponse( String requestId,
                                                             Map<String, Object> parameterValues){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        GetParameterValuesResponse response = new GetParameterValuesResponse();
        GetParameterValuesResponse.ParameterList paramList = new GetParameterValuesResponse.ParameterList();
        List<GetParameterValuesResponse.ParameterValueStruct> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameterValues.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();
            String xmlType = determineXmlType(paramValue);
            GetParameterValuesResponse.ParameterValueStruct param =
                    new GetParameterValuesResponse.ParameterValueStruct();
            param.setName(paramName);
            param.setValue(new GetParameterValuesResponse.TypedValue(paramValue, xmlType));
            params.add(param);
        }
        paramList.setParameters(params);
        response.setParameterList(paramList);
        body.setMethodResponse( response);
        envelope.setBody(body);
        return envelope;
    }


    public static Envelope createGetRpcMethodResponse( String requestId){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        GetRpcMethodResponse getRpcMethodResponse = new GetRpcMethodResponse();
        GetRpcMethodResponse.MethodList methodList = new GetRpcMethodResponse.MethodList();
        methodList.setMethods(CWMPElementManager.getFunctionList());
        getRpcMethodResponse.setMethodList(methodList);
        body.setMethodResponse(getRpcMethodResponse);
        envelope.setBody(body);
        return envelope;
    }

    public static Envelope createSetParameterAttributesResponse( String requestId){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();

        body.setMethodResponse(new SetParameterAttributesResponse());
        envelope.setBody(body);
        return envelope;
    }



    public static Envelope createSetParameterValuesResponse( String requestId,int state){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        SetParameterValuesResponse setParameterValuesResponse = new SetParameterValuesResponse(state);
        body.setMethodResponse(setParameterValuesResponse);
        envelope.setBody(body);
        return envelope;
    }

    public static Envelope createGetParameterNamesResponse( String requestId,String path,Integer nextLevel){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        List<GetParameterNamesResponse.ParameterInfoStruct> params=new ArrayList<>();
        if (!StringUtils.hasText(path)){//获取所有参数名称
            CWMPElementManager.getAllElementTreeNode().forEach(node->
                    params.add(
                            new GetParameterNamesResponse.ParameterInfoStruct(
                                    node.getFullName(),node.getValue().getPermission().getCode())
                    )
            );
        }else{
            CWMPElementManager.getElementTreeNode(path,nextLevel).forEach(node->
                    params.add(
                            new GetParameterNamesResponse.ParameterInfoStruct(
                                    node.getFullName(),node.getValue().getPermission().getCode())
                    )
            );
        }
        GetParameterNamesResponse getParameterNameResponse = new GetParameterNamesResponse(params);
        body.setMethodResponse(getParameterNameResponse);
        envelope.setBody(body);
        return envelope;
    }


    public static Envelope createFaultResponse( String requestId,
                                                int errorCode, String errorMessage){
        FalutResponseEnvelope falutResponseEnvelope = new FalutResponseEnvelope();
        FalutResponseEnvelope.Header header = new FalutResponseEnvelope.Header();
        FalutResponseEnvelope.ID id = new FalutResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        falutResponseEnvelope.setHeader(header);
        FalutResponseEnvelope.Body body = new FalutResponseEnvelope.Body();

        FalutResponseEnvelope.CwmpFault fault = new FalutResponseEnvelope.CwmpFault(errorCode,errorMessage);
        body.setFault(fault);
        falutResponseEnvelope.setBody(body);

        return falutResponseEnvelope;
    }
    public static Envelope createRebootResponse(String requestId) {
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        body.setMethodResponse(new RebootResponse());
        envelope.setBody(body);
        return envelope;
    }

    public static Envelope createFactoryResetResponse(String requestId){
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        body.setMethodResponse(new FactoryResetResponse());
        envelope.setBody(body);
        return envelope;
    }

    public static Envelope createUpLoadResponse(String requestId, Integer status, String startTime, String completeTime) {

        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        UploadResponse uploadResponse = new UploadResponse(status);
        uploadResponse.setStartTime(startTime);
        uploadResponse.setCompleteTime(completeTime);

        body.setMethodResponse(uploadResponse);
        envelope.setBody(body);
        return envelope;

    }

    public static Envelope createDownLoadResponse(String requestId, Integer status, String startTime, String completeTime,String commandKey) {

        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        DownloadResponse downloadResponse = new DownloadResponse(status);
        downloadResponse.setStartTime(startTime);
        downloadResponse.setCompleteTime(completeTime);
        downloadResponse.setCommandKey(commandKey);
        body.setMethodResponse(downloadResponse);
        envelope.setBody(body);
        return envelope;

    }


    public static Envelope createAddObjectResponse(String requestId, Integer status,Integer instanceNumber) {
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        AddObjectResponse addObjectResponse = new AddObjectResponse();
        addObjectResponse.setStatus(status);
        addObjectResponse.setInstanceNumber(instanceNumber);
        body.setMethodResponse(addObjectResponse);
        envelope.setBody(body);
        return envelope;
    }

    public static Envelope createDeleteObjectResponse(String requestId, int status) {
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(requestId);
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        DeleteObjectResponse deleteObjectResponse = new DeleteObjectResponse();
        deleteObjectResponse.setStatus(status);
        body.setMethodResponse(deleteObjectResponse);
        envelope.setBody(body);
        return envelope;
    }


    public static Envelope transferCompleteResponse( String startTime, String completeTime,String commandKey
            ,DownloadHandler.Result result,Long estimatedTotalSize,Long totalBytesSent,Long totalBytesReceived) {
        CPEMethodResponseEnvelope envelope = new CPEMethodResponseEnvelope();
        CPEMethodResponseEnvelope.Header header = new CPEMethodResponseEnvelope.Header();
        CPEMethodResponseEnvelope.ID id = new CPEMethodResponseEnvelope.ID();
        id.setValue(String.valueOf(count.addAndGet(1)));
        header.setId(id);
        envelope.setHeader(header);
        CPEMethodResponseEnvelope.Body body = new CPEMethodResponseEnvelope.Body();
        TransferCompleteResponse transferCompleteResponse = new TransferCompleteResponse();
        transferCompleteResponse.setCommandKey(commandKey);
        transferCompleteResponse.setStartTime(startTime);
        transferCompleteResponse.setCompleteTime(completeTime);
        transferCompleteResponse.setEstimatedTotalSize(estimatedTotalSize);
        transferCompleteResponse.setTotalBytesSent(totalBytesSent);
        transferCompleteResponse.setTotalBytesReceived(totalBytesReceived);
        transferCompleteResponse.setFaultStruct(new TransferCompleteResponse.FaultStruct(result.getCode(),result.getDesc()));
        body.setMethodResponse(transferCompleteResponse);
        envelope.setBody(body);
        return envelope;

    }

    public static String getCurrentTimeFormatted() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return now.format(formatter);
    }

    private static CPEInformRequestEnvelope.ParameterValueStruct createParam(String name, String value) {
        CPEInformRequestEnvelope.ParameterValueStruct p = new CPEInformRequestEnvelope.ParameterValueStruct();
        p.setName(name);
        CPEInformRequestEnvelope.Value val = new CPEInformRequestEnvelope.Value();
        val.setContent(value);
        p.setValue(val);
        return p;
    }


    public static Object determineValue(String value,String type) {
        if (!StringUtils.hasText( type)) {
            return value;
        }
        if (type.equalsIgnoreCase("xsd:boolean")) {
            return Boolean.valueOf( value);
        } else if (type.equalsIgnoreCase("xsd:int")) {
            return Integer.valueOf( value);
        } else if (type.equalsIgnoreCase("xsd:unsignedInt") || type.equalsIgnoreCase("xsd:long")) {
            return Long.valueOf( value);
        } else if (type.equalsIgnoreCase("xsd:base64")) {
            return Base64.getDecoder().decode(value);
        }else if (type.equalsIgnoreCase("xsd:dateTime")) {
            return convertXsdDateTimeToDate(value);
        } else {
            return value;
        }
    }

    private static Date convertXsdDateTimeToDate(String xsdDateTime) {
        if (xsdDateTime.contains("+") || xsdDateTime.contains("Z")) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(xsdDateTime, DateTimeFormatter.ISO_DATE_TIME);
            return Date.from(zonedDateTime.toInstant());
        } else {
            LocalDateTime localDateTime = LocalDateTime.parse(xsdDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    private static String determineXmlType(Object value) {
        if (value == null) {
            return "xsd:string";
        }
        if (value instanceof Boolean) {
            return "xsd:boolean";
        } else if (value instanceof Integer) {
            return "xsd:int";
        } else if (value instanceof Long) {
            return "xsd:unsignedInt";
        } else if (value instanceof Date) {
            return "xsd:dateTime";
        } else if (value instanceof byte[]) {
            return "xsd:base64";
        } else {
            return "xsd:string";
        }
    }



}
