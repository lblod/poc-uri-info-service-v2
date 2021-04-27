package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UriInfoService {
    private final SparqlClient sparqlClient;
    private final SparqlQueryStore sparqlQueryStore;

    public UriInfoService(SparqlClient sparqlClient, SparqlQueryStore sparqlQueryStore) {
        this.sparqlClient = sparqlClient;
        this.sparqlQueryStore = sparqlQueryStore;
    }

    private Model fetchSubject(String uri, List<String> uriProcessed) {
        if (StringUtils.isEmpty(uri) || uriProcessed.contains(uri)) {
            return ModelFactory.createDefaultModel();
        }
        String query = sparqlQueryStore.getQueryWithParameters("fetchSubject", Map.of("subject", uri));
        Model modelFromUri = sparqlClient.executeSelectQuery(query);
        uriProcessed.add(uri);
        Model model = modelFromUri.listStatements()
                .filterDrop(stmt -> stmt.getPredicate().equals(RDF.type) || !stmt.getObject().isURIResource())
                .toList()
                .stream()
                .sequential()
                .map(stmt -> stmt.getObject().asResource().getURI())
                .peek(iri -> log.debug("uri {}", iri))
                .map(iri -> this.fetchSubject(iri, uriProcessed))
                .reduce(Model::add)
                .orElseGet(ModelFactory::createDefaultModel);
        return ModelFactory.createUnion(modelFromUri, model);
    }

    public Model fetchSubject(String uri) {
        return fetchSubject(uri, new ArrayList<>());
    }

    public String fetchPageUri(String uri) {
        String query = sparqlQueryStore.getQueryWithParameters("fetchPageUri", Map.of("uri", uri));
        return sparqlClient.executeSelectQuery(query, resultSet -> {
            if (resultSet.hasNext()) {
                return resultSet.next().get("uri").asResource().getURI();
            }
            return null;
        });
    }

    public Model fetchPage(String uri) {
        var pageUri = this.fetchPageUri(uri);
        return this.fetchSubject(pageUri);
    }

}
