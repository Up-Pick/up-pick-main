package org.oneog.uppick.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
@EnableBatchProcessing
public class UppickBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(UppickBatchApplication.class, args);
    }

}
