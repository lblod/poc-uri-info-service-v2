package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.uriinfo.v2.app.error.SubjectUriNotFoundException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

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
    String query = sparqlQueryStore.getQueryWithParameters("dynamicQuery", Map.of("rootSubject", rootSubject, "variables", variablesQuery, "typeUri", rootTypeUri));

    log.info(query);
    return this.sparqlClient.executeSelectQuery(query, resultSet -> {
      List<Map<String, String>> res = new ArrayList<>();
      resultSet.forEachRemaining(querySolution -> {
        Map<String, String> values = new LinkedHashMap<>();
        querySolution.varNames().forEachRemaining(s -> {
          RDFNode rdfNode = querySolution.get(s);
          if (rdfNode.isLiteral()) {
            values.put(s, rdfNode.asLiteral().getString());
          }
          else {
            values.put(s, rdfNode.asResource().getURI());
          }
        });
        res.add(values);
      });
      return res;
    });
  }

  public Map<String, String> fetchSource(String subject, String predicate, String type) {
    String query = sparqlQueryStore.getQueryWithParameters("fetchSource",
                                                           Map.of("subject", subject, "predicate", predicate, "type", type));
    log.info(query);
    return this.sparqlClient.executeSelectQuery(query, resultSet -> {
      if (!resultSet.hasNext()) {
        return Map.of();
      }
      Map<String, String> values = new HashMap<>();
      QuerySolution solution = resultSet.next(); // todo maybe we could have multiple values, dunno
      ofNullable(solution.get("type")).map(RDFNode::asResource).map(Resource::getURI).ifPresent(uri -> values.put("type",uri));
      ofNullable(solution.get("subject")).map(RDFNode::asResource).map(Resource::getURI).ifPresent(uri -> values.put("subject",uri));
      return values;
    });

  }
}
