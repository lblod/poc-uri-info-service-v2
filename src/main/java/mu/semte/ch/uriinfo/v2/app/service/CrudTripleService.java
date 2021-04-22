package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import mu.semte.ch.uriinfo.v2.lib.utils.SparqlClient;
import mu.semte.ch.uriinfo.v2.lib.utils.SparqlQueryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CrudTripleService {
    private final SparqlQueryStore queryStore;
    private final SparqlClient client;
    @Value("${sparql.defaultGraphUri}")
    private String defaultGraphUri;

    public CrudTripleService(SparqlQueryStore queryStore, SparqlClient client) {
        this.queryStore = queryStore;
        this.client = client;
    }

    public List<FrontendStmt> updateTriples(List<FrontendStmt> triples) {
        String query = queryStore.getQueryWithParameters("updateTriples", Map.of("triples", triples, "graph", defaultGraphUri));
        log.debug(query);
        client.executeUpdateQuery(query);
        return triples;
    }

    public List<FrontendStmt> insertTriples(List<FrontendStmt> triples) {
        String query = queryStore.getQueryWithParameters("insertTriples", Map.of("triples", triples, "graph", defaultGraphUri));
        log.debug(query);
        client.executeUpdateQuery(query);
        return triples;
    }
}
