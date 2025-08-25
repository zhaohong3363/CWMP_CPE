package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.enums.DownloadStatus;
import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.enums.FileType;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.Download;
import cn.sct.networkmanager.agent.protocol.cwmp.CommandKeyFileSystemManager;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DownloadHandler implements Handler {
    private static final Logger log = LoggerFactory.getLogger(DownloadHandler.class);
    private final HttpMessageCreateFactory httpMessageCreateFactory;
    private final String FILE_DIRECTORY = "cwmp/download";


    protected DownloadHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;

    }
    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        Download methodRequest = (Download) envelope.getBody().getMethodRequest();
        String fileUrl = methodRequest.getUrl();
        String fileName = methodRequest.getTargetFileName();
        String commandKey = methodRequest.getCommandKey();
        Integer delaySeconds = methodRequest.getDelaySeconds();
        String startTime = SoapCreateFactory.getCurrentTimeFormatted();
        if (!isSupported(methodRequest.getFileType())) {
            log.warn("不支持文件类型");
            //不支持文件类型
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),FaultCode.FILE_TRANSFER_FAILED.getCode(),"Unsupported file type"),session);
        }
        Mono.delay(Duration.ofSeconds(Math.max(delaySeconds, 3)))
                .flatMap(i -> {
                    try{
                        Path path = downloadFile0(fileUrl, FILE_DIRECTORY, fileName);
                        return dealFile(FileType.fromFiletype(methodRequest.getFileType()),path, methodRequest.getFileSize())
                                .flatMap(result -> {
                                    if (result.isReboot()){
                                        Mono.delay(Duration.ofSeconds(30)).flatMap(ig -> {
                                                    Disposable timeOutTask = Mono.delay(Duration.ofSeconds(30)).doOnNext(x -> {
                                                        log.info("{}:重启设备超时", System.currentTimeMillis());
                                                        session.disconnect();
                                                    }).subscribeOn(Schedulers.boundedElastic()).subscribe();;
                                                    while (!session.isAlive() ) {}
                                                    timeOutTask.dispose();
                                                    return this.reboot();
                                                }).flatMap(flag->{
                                                    if (flag){//重启成功
                                                        return session.connect(CWMPEventCode.MDownload,commandKey).flatMap(aBoolean -> {
                                                            if (aBoolean){
                                                                return session.send(httpMessageCreateFactory.createNull(),
                                                                        new AtomicInteger(0));
                                                            }
                                                            return Mono.empty().then();
                                                        });
                                                    }
                                                    return Mono.empty().then();
                                                })
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .subscribe();
                                    }

                                        return session.connect(CWMPEventCode.TRANSFER_COMPLETE
                                                ,commandKey).flatMap(aBoolean -> {
                                                    if (aBoolean){
                                                        return responseMessage(
                                                                SoapCreateFactory.transferCompleteResponse(startTime
                                                                        ,SoapCreateFactory.getCurrentTimeFormatted(),commandKey,result
                                                                        , methodRequest.getFileSize(), 0L, methodRequest.getFileSize())
                                                        ,session);//发送TransferComplete 报文
                                                    }
                                                    return Mono.empty().then();
                                        });
                                });
                    }catch (IOException e){
                        log.warn("下载文件失败",e);
                        //下载文件失败
                        return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),FaultCode.FILE_TRANSFER_FAILED.getCode(),"file download fail"),session);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();


        return session.send(httpMessageCreateFactory
                        .createHttpRequest(SoapCreateFactory.createDownLoadResponse(envelope.getId(),
                                DownloadStatus.UNSUPPORTED_FILE_TYPE.getCode(), startTime,
                                SoapCreateFactory.getCurrentTimeFormatted(), commandKey))
                , new AtomicInteger(1));
    }

    public abstract boolean isSupported(String name);

    public Path downloadFile0(String fileUrl, String targetDirectory, String fileName)
            throws IOException {
        URL urlobj = new URL(fileUrl);
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        Path directory = Paths.get(targetDirectory);
        Path targetFile = directory.resolve(fileName);
        try {
            rbc = Channels.newChannel(urlobj.openStream());
            fos = new FileOutputStream(targetFile.toString());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } finally {
            if (rbc != null) rbc.close();
            if (fos != null) fos.close();
        }
        return targetFile;
    }

    /**
     * 下完文件做完响应操作后返回结果，和描述，比如固件升级等，会将结果反馈到acs
     *
     *
     * **/
    public abstract Mono<Result> dealFile(FileType fileType, Path filePath, Long fileSize);

    public Mono<Boolean> reboot() {
        return Mono.just(true);
    }
    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(Download.class);
    }

    @Override
    public String getFunctionName() {
        return "Download";
    }

    /**
     * 是否需要重启，如果需要重启，返回 isReboot返回true, 调用reboot方法
     * **/
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Result {
        private int code;
        private String desc;
        private boolean isReboot;
    }

}
