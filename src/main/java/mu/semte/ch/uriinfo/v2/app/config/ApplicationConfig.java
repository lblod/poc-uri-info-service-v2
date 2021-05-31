package mu.semte.ch.uriinfo.v2.app.config;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.config.CoreConfig;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;

@Configuration
@Import(CoreConfig.class)
@Slf4j
public class ApplicationConfig {

  @Value("${sparql.metaDatabaseDirectory}")
  private String metaDatabaseDirectory;

  @Bean
  public Slugify slugify() {
    return new Slugify();
  }

  @Bean(destroyMethod = "close")
  public Dataset metaModelDataset() {
   File dir = new File(metaDatabaseDirectory);
   if(!dir.exists()){
     dir.mkdirs();
   }
   return TDBFactory.createDataset(metaDatabaseDirectory);
  }

}
