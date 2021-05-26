package mu.semte.ch.uriinfo.v2.app.service;

import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.v2.FrontendMenuLink;
import mu.semte.ch.uriinfo.v2.app.dto.v2.FrontendUI;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UIBuilderServiceV2Test {
    @Autowired
    private InMemoryTripleStoreService inMemoryTripleStoreService;

    @Autowired
    private UIBuilderServiceV2 uiBuilderServiceV2;

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
        List<FrontendMenuLink> frontendMenuLinks = uiBuilderServiceV2.buildMenu(namedModel, "http://mu.semte.ch/vocabularies/ext/contact_gegevens");
        assertEquals(3, frontendMenuLinks.size());
        FrontendMenuLink personlijkGegevensPage = frontendMenuLinks.get(0);
        assertEquals("http://mu.semte.ch/vocabularies/ext/personlijke_gegevens", personlijkGegevensPage.getUri());
        assertFalse(personlijkGegevensPage.isActive());
        assertTrue(personlijkGegevensPage.isMainPage());
        assertEquals("Persoonlijke gegevens", personlijkGegevensPage.getLabel());

        FrontendMenuLink contactGegevensPage = frontendMenuLinks.get(1);
        assertTrue(contactGegevensPage.isActive());

    }

    @Test//todo ignore integration test
    void build(){
        String personUri = "http://data.lblod.info/id/persoon/5b18df411cbb975f6b57853018306250";
        FrontendUI build = this.uiBuilderServiceV2.build(personUri, null);
        System.out.println(build);
    }
}