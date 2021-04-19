package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import mu.semte.ch.uriinfo.v2.lib.utils.SparqlClient;
import mu.semte.ch.uriinfo.v2.lib.utils.SparqlQueryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class UpdateTripleService {
  private final SparqlQueryStore queryStore;
  private final SparqlClient client;
  @Value("${sparql.defaultGraphUri}")
  private String defaultGraphUri;

  public UpdateTripleService(SparqlQueryStore queryStore, SparqlClient client) {
    this.queryStore = queryStore;
    this.client = client;
  }

  public FrontendStmt update(FrontendStmt triple) {
    String query = queryStore.getQueryWithParameters("updateTriple", Map.of("triple", triple, "graph", defaultGraphUri));
    log.debug(query);
    client.executeUpdateQuery(query);
    return triple;
  }
}
