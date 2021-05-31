package mu.semte.ch.uriinfo.v2.app.config;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.service.InMemoryTripleStoreService;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@Slf4j
public class MetaLoaderRouteConfig extends RouteBuilder {

  @Value("${sparql.metaLoaderDirectory}")
  private String metaLoaderDirectory;

  private final InMemoryTripleStoreService inMemoryTripleStoreService;

  public MetaLoaderRouteConfig(InMemoryTripleStoreService inMemoryTripleStoreService) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
  }

  @Override
  public void configure() throws Exception {
    fromF("file:%s?charset=utf-8", metaLoaderDirectory)
            .filter(method(this, "isTurtle"))
            .convertBodyTo(byte[].class)
            .bean(()-> this, "process");
  }

  public boolean isTurtle(@Header(Exchange.FILE_NAME) String fileName){
    return "ttl".equals(FilenameUtils.getExtension(fileName));
  }
  public void process(@Header(Exchange.FILE_NAME) String fileName, @Body byte[] file){
    log.info("process file {}", fileName);
    Model model = ModelUtils.toModel(new ByteArrayInputStream(file), Lang.TTL);
    Statement property = model.getProperty(null, FrontendVoc.P_CONTENT_TYPE);
    String namedGraph = property.getResource().getURI();
    log.info("process model for type {}",namedGraph );
    inMemoryTripleStoreService.write(namedGraph, model);
    log.info("done");
  }
}
