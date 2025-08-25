package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.enums.FileType;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.Upload;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class UploadHandler implements Handler {
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    protected UploadHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        Upload methodRequest = (Upload) envelope.getBody().getMethodRequest();
        if (!isSupported(methodRequest.getFileType())){
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), "no supported CommandKey"), session);
        }
        String url = methodRequest.getUrl();
        return Mono
                .delay(Duration.ofSeconds(Math.max(3,methodRequest.getDelaySeconds())))
                .flatMap(i->{
                    String startTime = SoapCreateFactory.getCurrentTimeFormatted();
                    try {
                        HttpResponse<String> stringHttpResponse = uploadFile0(url,
                                uploadFile(FileType.fromFiletype(methodRequest.getFileType())));
                        if (stringHttpResponse.statusCode() != 200){
                            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), "no supported upload file type"), session);
                        }
                    } catch (Exception e) {
                        return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                                FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), "no supported upload file type"), session);
                    }
                    return responseMessage(SoapCreateFactory.createUpLoadResponse(envelope.getId(), 0,startTime,SoapCreateFactory.getCurrentTimeFormatted()), session);
                });
    }



    @Override
    public String getFunctionName() {
        return "Upload";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
          return tClass.equals(Upload.class);
    }

    public abstract boolean isSupported(String name);

    public  HttpResponse<String> uploadFile0(String url, Path filePath) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofFile(filePath))
                .build();

      return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public abstract Path uploadFile(FileType fileType) throws Exception;
}
