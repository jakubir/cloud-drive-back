package pl.jakubirla.clouddrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.jakubirla.clouddrive.config.RsaKeyProperties;
import pl.jakubirla.clouddrive.config.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({RsaKeyProperties.class, StorageProperties.class})
public class CloudDriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudDriveApplication.class, args);
    }
}
