package mu.semte.ch.uriinfo.v2.lib.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFLanguages;

import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class SparqlClient {
  private String url;
  private HttpClient httpClient;

  public void insertModel(String graphUri, Model model) {
    var triples = ModelUtils.toString(model, RDFLanguages.NTRIPLES);
    String updateQuery = String.format("INSERT DATA { GRAPH <%s> { %s } }", graphUri, triples);
    executeUpdateQuery(updateQuery);
  }

  @SneakyThrows
  public void executeUpdateQuery(String updateQuery) {
    log.debug(updateQuery);
    try (RDFConnection conn = RDFConnectionRemote.create()
                                                 .destination(url)
                                                 .httpClient(httpClient)
                                                 .build()) {
      conn.update(updateQuery);
    }

  }

  public <R> R executeSelectQuery(String query, Function<ResultSet, R> resultHandler) {
    log.debug(query);
    try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(url, query, httpClient)) {
      return resultHandler.apply(queryExecution.execSelect());
    }
  }

  public Model executeSelectQuery(String query) {
    return executeSelectQuery(query, resultSet -> {
      Model model = ModelFactory.createDefaultModel();
      resultSet.forEachRemaining(querySolution -> {
        RDFNode subject = querySolution.get("s");
        RDFNode predicate = querySolution.get("p");
        RDFNode object = querySolution.get("o");
        var triple = Triple.create(subject.asNode(), predicate.asNode(), object.asNode());
        model.getGraph().add(triple);
      });
      return model;
    });
  }

  public boolean executeAskQuery(String askQuery) {
    log.debug(askQuery);
    try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(url, askQuery, httpClient)) {
      return queryExecution.execAsk();
    }
  }

  public void dropGraph(String graphUri) {
    executeUpdateQuery("clear graph <" + graphUri + ">");
  }


}
