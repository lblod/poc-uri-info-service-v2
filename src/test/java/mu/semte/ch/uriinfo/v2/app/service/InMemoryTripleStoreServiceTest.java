package mu.semte.ch.uriinfo.v2.app.service;

import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InMemoryTripleStoreServiceTest {

    @Autowired
    private InMemoryTripleStoreService inMemoryTripleStoreService;

    @Value("classpath:new-meta-model.ttl")
    private Resource newMetaModel;

    @Test
    void write() throws IOException {
        Model model = ModelUtils.toModel(newMetaModel.getInputStream(), Lang.TTL);
        Statement property = model.getProperty(null, FrontendVoc.P_CONTENT_TYPE);
        String namedGraph = property.getResource().getURI();
        assertEquals("http://www.w3.org/ns/person#Person", namedGraph);
        inMemoryTripleStoreService.write(namedGraph, model);
        Model namedModel = inMemoryTripleStoreService.getNamedModel(namedGraph);
        assertNotNull(namedModel);
        assertFalse(namedModel.isEmpty());
        assertEquals(model.size(), namedModel.size());
        assertTrue(ModelUtils.difference(model, namedModel).isEmpty());
    }

    @Test
    void getNamedModel() {
    }
}