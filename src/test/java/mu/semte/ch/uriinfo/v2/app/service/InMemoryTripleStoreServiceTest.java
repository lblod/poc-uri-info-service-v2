package mu.semte.ch.uriinfo.v2.app.service;

import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InMemoryTripleStoreServiceTest {

    @Autowired
    private InMemoryTripleStoreService inMemoryTripleStoreService;

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
    void writeTest() throws IOException {
        Model namedModel = inMemoryTripleStoreService.getNamedModel(NAMED_GRAPH);
        assertNotNull(namedModel);
        assertFalse(namedModel.isEmpty());
    }

    @Test
    void getNamedModel() {
        Model namedModel = inMemoryTripleStoreService.getNamedModel(NAMED_GRAPH);
        var pages = namedModel.getRequiredProperty(null, FrontendVoc.P_PAGES);
        RDFList list = namedModel.getList(pages.getObject().asResource());
        assertEquals(3, list.size());
    }
}
