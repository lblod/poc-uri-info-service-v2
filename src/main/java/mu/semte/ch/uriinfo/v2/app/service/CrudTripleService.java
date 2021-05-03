package mu.semte.ch.uriinfo.v2.app.service;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.SparqlClient;
import mu.semte.ch.lib.utils.SparqlQueryStore;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendField;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CrudTripleService {
    private final SparqlQueryStore queryStore;
    private final UriInfoService uriInfoService;
    private final SparqlClient client;
    @Value("${sparql.defaultGraphUri}")
    private String defaultGraphUri;

    public CrudTripleService(SparqlQueryStore queryStore, UriInfoService uriInfoService, SparqlClient client) {
        this.queryStore = queryStore;
        this.uriInfoService = uriInfoService;
        this.client = client;
    }

    public List<FrontendStmt> updateTriples(List<FrontendStmt> triples) {
        String query = queryStore.getQueryWithParameters("updateTriples", Map.of("triples", triples, "graph", defaultGraphUri));
        log.debug(query);
        client.executeUpdateQuery(query);
        return triples;
    }

    public Map<String, String> listOptions(FrontendField field) {
        Preconditions.checkArgument(field.isList(), "This field is not a list! Check the meta model");
        Preconditions.checkArgument(!field.getTriples().isEmpty(), "At least 1 triple should be present! Check the  model");
        FrontendStmt triple = field.getTriples().get(0);
        Preconditions.checkNotNull(triple.getSubject(), "Subject cannot be null in triple! Check the  model");
        Preconditions.checkArgument(field.getTriples().stream().allMatch(t -> triple.getSubject().equals(t.getSubject())), "All the triples must come from the same source! Check the  model");
        Model model = uriInfoService.fetchSubject(field.getTypeUri());
        var target = model.getRequiredProperty(null,FrontendVoc.P_TARGET).getResource();
        String query = queryStore.getQueryWithParameters("listOptions", Map.of(
                "label", triple.getPredicate(),
                "subject", triple.getSubject(),
                "target", target.getURI()
        ));
        log.debug(query);
        return client.executeSelectQuery(query, rs ->{
            Map<String , String> results = new HashMap<>();
            rs.forEachRemaining(querySolution -> results.put(querySolution.getResource("option").getURI(), querySolution.getLiteral("label").getValue().toString()));
            return results;
        });
    }

    public List<FrontendStmt> insertTriples(List<FrontendStmt> triples) {
        String query = queryStore.getQueryWithParameters("insertTriples", Map.of("triples", triples, "graph", defaultGraphUri));
        log.debug(query);
        client.executeUpdateQuery(query);
        return triples;
    }
}
