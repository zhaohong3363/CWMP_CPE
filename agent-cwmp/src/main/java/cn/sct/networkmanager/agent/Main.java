package cn.sct.networkmanager.agent;

import cn.sct.networkmanager.agent.annotation.EnableCWMPAgent;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.sct")
@EnableCWMPAgent
public class Main {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(Main.class, args);
    }
}