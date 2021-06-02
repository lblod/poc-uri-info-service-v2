package mu.semte.ch.uriinfo.v2.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendForm;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendFormRequest;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelExtract;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.impl.SelectorImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_RML;

@Service
@Slf4j
public class FormService {
  private final InMemoryTripleStoreService inMemoryTripleStoreService;

  public FormService(InMemoryTripleStoreService inMemoryTripleStoreService) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
  }

  public void persist(FrontendFormRequest request) throws JsonProcessingException {
    ObjectNode skeleton = request.getSkeleton();
    String source = new ObjectMapper().writeValueAsString(skeleton);
    log.info(source);
    var metaModel = inMemoryTripleStoreService.getNamedModel(request.getTypeUri());
    var rmlModel = ModelFactory.createDefaultModel();
    metaModel.listObjectsOfProperty(ResourceFactory.createProperty(request.getFormUri()), P_RML)
             .forEach(r-> UtilService.extractFromModel( r.asResource(), metaModel, rmlModel));

    String rmlMapping = ModelUtils.toString(rmlModel, Lang.TURTLE);

    // rml
    var loader = RmlMappingLoader
            .build();
    var mapper = RmlMapper.newBuilder()
                          .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                          .classPathResolver("/tmp")
                          .build();
    mapper.bindInputStream("input", IOUtils.toInputStream(source, StandardCharsets.UTF_8));
    var mapping = loader.load(RDFFormat.TURTLE, IOUtils.toInputStream(rmlMapping, StandardCharsets.UTF_8));
    var result = mapper.map(mapping);
    StringWriter writer = new StringWriter();
    Rio.write(result, writer, RDFFormat.TURTLE);
    log.info(writer.toString());
  }



}
