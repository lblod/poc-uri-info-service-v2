package mu.semte.ch.uriinfo.v2.app.config;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
// TODO if the lib package is extracted to make some kind of mu-java-template, you may want to uncomment this
//@Import(SparqlConfig.class)
//@Import(ExceptionHandler.class)
@Slf4j
public class ApplicationConfig {

  @Bean
  public Slugify slugify() {
    return new Slugify();
  }

}
