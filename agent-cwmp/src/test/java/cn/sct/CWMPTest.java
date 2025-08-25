package cn.sct;

import cn.sct.networkmanager.agent.annotation.EnableCWMPAgent;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCWMPAgent
@SpringBootApplication(scanBasePackages = "cn.sct")
public class CWMPTest {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(CWMPTest.class, args);
    }
}
