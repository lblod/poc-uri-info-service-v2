package mu.semte.ch.uriinfo.v2.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendMenuLink;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
//@Disabled //todo ignore integration test
class UIBuilderServiceTest {
  @Autowired
  private InMemoryTripleStoreService inMemoryTripleStoreService;

  @Autowired
  private UIBuilderService uiBuilderService;

  @Value("classpath:new-meta-model.ttl")
  private Resource newMetaModel;

  static String NAMED_GRAPH = "http://www.w3.org/ns/person#Person";

  @BeforeAll
  void setup() throws IOException {
    Model model = ModelUtils.toModel(newMetaModel.getInputStream(), Lang.TTL);
    Statement property = model.getProperty(null, FrontendVoc.P_CONTENT_TYPE);
    String namedGraph = property.getResource().getURI();
    inMemoryTripleStoreService.write(namedGraph, model);
  }

  @Test
  void buildMenu() {
    Model namedModel = inMemoryTripleStoreService.getNamedModel(NAMED_GRAPH);
    List<FrontendMenuLink> frontendMenuLinks = uiBuilderService.buildMenu(namedModel, "http://mu.semte.ch/vocabularies/ext/contact_gegevens");
    assertEquals(3, frontendMenuLinks.size());
    FrontendMenuLink personlijkGegevensPage = frontendMenuLinks.get(0);
    assertEquals("http://mu.semte.ch/vocabularies/ext/personlijke_gegevens", personlijkGegevensPage.getUri());
    assertFalse(personlijkGegevensPage.isActive());
    assertTrue(personlijkGegevensPage.isMainPage());
    assertEquals("Persoonlijke gegevens", personlijkGegevensPage.getLabel());

    FrontendMenuLink contactGegevensPage = frontendMenuLinks.get(1);
    assertTrue(contactGegevensPage.isActive());

  }

  @Test
  void build() throws IOException {
    String personUri = "http://data.lblod.info/id/persoon/5b18df411cbb975f6b57853018306250";
    FrontendUI ui = this.uiBuilderService.build(personUri, null);
    assertEquals(3, ui.getMenu().size());
    assertNotNull(ui.getPage());
    assertEquals("Persoonlijke gegevens: Luuk Van Eylen", ui.getPage().getTitle());
    assertEquals("Luuk Van Eylen", ui.getPage().getSubtitle());
    assertEquals(0, ui.getPage().getOrdering());

    log.warn(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ui));
  }

  @Test
  void buildTitle() {

  }
}
