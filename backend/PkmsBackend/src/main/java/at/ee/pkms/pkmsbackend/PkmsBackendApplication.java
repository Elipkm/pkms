package at.ee.pkms.pkmsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PkmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PkmsBackendApplication.class, args);
    }

}
