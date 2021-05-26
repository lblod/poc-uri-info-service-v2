package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.uriinfo.v2.app.error.SubjectUriNotFoundException;
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
public class UriInfoServiceV2 {
    private final SparqlClient sparqlClient;
    private final SparqlQueryStore sparqlQueryStore;

    public UriInfoServiceV2(SparqlClient sparqlClient, SparqlQueryStore sparqlQueryStore) {
        this.sparqlClient = sparqlClient;
        this.sparqlQueryStore = sparqlQueryStore;
    }
    public String fetchType(String uri) {
        String query = sparqlQueryStore.getQueryWithParameters("fetchType", Map.of("uri", uri));
        return sparqlClient.executeSelectQuery(query, resultSet -> {
            if (resultSet.hasNext()) {
                return resultSet.next().get("type").asResource().getURI();
            }
            throw new SubjectUriNotFoundException();
        });
    }


}
