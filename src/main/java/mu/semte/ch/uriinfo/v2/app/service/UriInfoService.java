package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.uriinfo.v2.app.error.SubjectUriNotFoundException;
import org.apache.jena.rdf.model.RDFNode;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class UriInfoService {
    private final SparqlClient sparqlClient;
    private final SparqlQueryStore sparqlQueryStore;

    public UriInfoService(SparqlClient sparqlClient, SparqlQueryStore sparqlQueryStore) {
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


    public List<Map<String, String>> dynamicQuery(String rootSubject, String rootTypeUri, Map<String, String> variablesQuery) {
        String query = sparqlQueryStore.getQueryWithParameters("dynamicQuery", Map.of("rootSubject", rootSubject, "variables", variablesQuery,"typeUri",rootTypeUri));

        log.info(query);
        return this.sparqlClient.executeSelectQuery(query, resultSet -> {
            List<Map<String, String>> res = new ArrayList<>();
           resultSet.forEachRemaining(querySolution -> {
           Map<String, String> values = new LinkedHashMap<>();
               querySolution.varNames().forEachRemaining(s -> {
                   RDFNode rdfNode = querySolution.get(s);
                   if(rdfNode.isLiteral()){
                       values.put(s, rdfNode.asLiteral().getString());
                   } else{
                       values.put(s,rdfNode.asResource().getURI());
                   }
               });
               res.add(values);
           });
           return res;
        });
    }
}
