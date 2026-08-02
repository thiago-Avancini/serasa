package br.com.serasa.scalesimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ScaleSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaleSimulatorApplication.class, args);
    }
}