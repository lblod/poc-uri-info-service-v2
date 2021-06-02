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
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendFormRequest;
import mu.semte.ch.uriinfo.v2.app.dto.form.Triple;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_EDIT_FORM;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_RML;

@Service
@Slf4j
public class FormService {
  private final InMemoryTripleStoreService inMemoryTripleStoreService;
  private final SparqlClient sparqlClient;
  private final SparqlQueryStore queryStore;

  @Value("${sparql.defaultGraphUri}")
  private String defaultGraphUri;
  public FormService(InMemoryTripleStoreService inMemoryTripleStoreService,
                     SparqlClient sparqlClient,
                     SparqlQueryStore queryStore) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
    this.sparqlClient = sparqlClient;
    this.queryStore = queryStore;
  }

  public String persist(FrontendFormRequest request) throws JsonProcessingException {
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

    Model model = ModelUtils.toModel(writer.toString(), "TTL");
     model.listStatements().toList().stream()
         .map(stmt -> Triple.builder().subject(stmt.getSubject().getURI()).predicate(stmt.getPredicate().getURI()).build())
         .forEach(triple ->{
           String queryWithParameters = queryStore.getQueryWithParameters("deleteTriples", Map.of("graph", defaultGraphUri,"triple", triple));
           sparqlClient.executeUpdateQuery(queryWithParameters);
         });

    sparqlClient.insertModel(defaultGraphUri, model);
    Resource resource = metaModel.listSubjectsWithProperty(P_EDIT_FORM, ResourceFactory.createResource(request.getFormUri()))
                                 .toList()
                                 .stream().findFirst().orElseThrow(()-> new RuntimeException("unexpected error"));
    return resource.getURI();
  }



}
