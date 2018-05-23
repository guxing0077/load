package com.load;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class LoadApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadApplication.class, args);
    }
}
