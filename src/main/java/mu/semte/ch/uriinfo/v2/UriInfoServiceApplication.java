package mu.semte.ch.uriinfo.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UriInfoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UriInfoServiceApplication.class, args);
    }

}
