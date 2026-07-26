package com.dcasset.accessory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AccessoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccessoryServiceApplication.class, args);
    }
}
