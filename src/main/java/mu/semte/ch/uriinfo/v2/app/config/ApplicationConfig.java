package mu.semte.ch.uriinfo.v2.app.config;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.config.CoreConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CoreConfig.class)
@Slf4j
public class ApplicationConfig {

    @Bean
    public Slugify slugify() {
        return new Slugify();
    }

}
