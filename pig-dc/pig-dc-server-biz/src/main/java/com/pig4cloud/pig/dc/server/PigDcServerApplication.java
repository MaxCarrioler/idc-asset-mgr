package com.pig4cloud.pig.dc.server;

import com.pig4cloud.pig.common.feign.annotation.EnablePigFeignClients;
import com.pig4cloud.pig.common.security.annotation.EnablePigResourceServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnablePigResourceServer
@EnablePigFeignClients
public class PigDcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PigDcServerApplication.class, args);
    }
}
