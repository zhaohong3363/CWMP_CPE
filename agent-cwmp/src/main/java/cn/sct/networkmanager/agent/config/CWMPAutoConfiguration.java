package cn.sct.networkmanager.agent.config;

import cn.sct.networkmanager.agent.CWMPAgent;
import cn.sct.networkmanager.agent.domain.enums.FileType;
import cn.sct.networkmanager.agent.domain.model.acs.AddObject;
import cn.sct.networkmanager.agent.domain.model.acs.DeleteObject;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.*;
import cn.sct.networkmanager.agent.element.RegisterCWMPElement;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(CPEConfigProperties.class)
public class CWMPAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CWMPAutoConfiguration.class);

    @Bean
    public HttpMessageCreateFactory httpMessageCreateFactory(CPEConfigProperties cpeConfigProperties){
        return new HttpMessageCreateFactory(cpeConfigProperties);
    }
    @Bean
    public DefaultCwmpSession createDefaultCwmpSession(CPEConfigProperties cpeConfigProperties
            , ApplicationEventPublisher eventPublisher, HttpMessageCreateFactory httpMessageCreateFactory, ApplicationContext context){
        return new DefaultCwmpSession(cpeConfigProperties,httpMessageCreateFactory,eventPublisher,context);
    }

    @Bean
    public RegisterCWMPElement registerCWMPElement(CPEConfigProperties cpeConfigProperties){
        return new RegisterCWMPElement(cpeConfigProperties);
    }

    @Bean
    public GetParameterValuesMethodHandler getParameterValuesMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new GetParameterValuesMethodHandler(httpMessageCreateFactory);
    }
    @Bean
    public GetRPCMethodsHandler getRPCMethodsHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new GetRPCMethodsHandler(httpMessageCreateFactory);
    }

    @Bean
    public SetParameterValueMethodsHandler setParameterValueMethodsHandler(HttpMessageCreateFactory httpMessageCreateFactory,ApplicationEventPublisher eventPublisher){
        return new SetParameterValueMethodsHandler(httpMessageCreateFactory,eventPublisher);
    }


    @Bean
    public GetParameterNamesMethodHandler getParameterNamesMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new GetParameterNamesMethodHandler(httpMessageCreateFactory);
    }

    @Bean
    public RebootMethodHandler rebootMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new RebootMethodHandler(httpMessageCreateFactory) {
            @Override
            public HttpMessageCreateFactory getHttpMessageCreateFactory() {
                return httpMessageCreateFactory;
            }

            @Override
            public Mono<Boolean> reboot() {
                log.info("RebootMethodHandler reboot");
                return Mono.just(true);
            }
        };
    }

    @Bean
    public FactoryResetMethodHandler factoryResetMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new FactoryResetMethodHandler(httpMessageCreateFactory) {
            @Override
            public void reset() {
                log.info("FactoryResetMethodHandler reset");
            }
        };
    }

    @Bean
    public UploadHandler uploadHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new UploadHandler(httpMessageCreateFactory) {
            @Override
            public HttpMessageCreateFactory getHttpMessageCreateFactory() {
                return httpMessageCreateFactory;
            }

            @Override
            public boolean isSupported(String name) {
                return true;
            }

            @Override
            public Path uploadFile(FileType fileType) throws Exception {

                return   Paths.get("test.cfg") ;
            }
        };
    }
    @Bean
    public DownloadHandler downloadHandler(HttpMessageCreateFactory httpMessageCreateFactory) throws IOException {
        return new DownloadHandler(httpMessageCreateFactory) {
            @Override
            public HttpMessageCreateFactory getHttpMessageCreateFactory() {
                return httpMessageCreateFactory;
            }

            @Override
            public boolean isSupported(String name) {
                return true;
            }

            @Override
            public Mono<Result> dealFile(FileType fileType, Path filePath,Long fileSize) {
                log.info("DownloadHandler dealFile");
                return Mono.just(new Result(0,"success",false));
            }
        };
    }
    @Bean
    public AddObjectHandler addObjectHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new AddObjectHandler(httpMessageCreateFactory) {
            @Override
            public HttpMessageCreateFactory getHttpMessageCreateFactory() {
                return httpMessageCreateFactory;
            }

            @Override
            public Mono<Result> add(AddObject addObject) {
                log.info("AddObjectHandler add");
                return Mono.just(new Result(true,5));
            }
        };
    }

    @Bean
    public DeleteObjectHandler deleteObjectHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new DeleteObjectHandler(httpMessageCreateFactory) {
            @Override
            public HttpMessageCreateFactory getHttpMessageCreateFactory() {
                return httpMessageCreateFactory;
            }

            @Override
            public Mono<Boolean> delete(DeleteObject deleteObject) {
                log.warn("DeleteObjectHandler delete");
                return Mono.just(true);
            }


        };
    }

    @Bean
    public SetParameterAttributesHandler setParameterAttributesHandler(HttpMessageCreateFactory httpMessageCreateFactory){
        return new SetParameterAttributesHandler(httpMessageCreateFactory);
    }

    @Bean
    public CWMPAgent cwmpAgent(DefaultCwmpSession defaultCwmpSession,
                               HttpMessageCreateFactory httpMessageCreateFactory,
                               CPEConfigProperties cpeConfigProperties,ApplicationContext context){
        return new CWMPAgent(defaultCwmpSession,cpeConfigProperties,httpMessageCreateFactory,context);
    }


}
