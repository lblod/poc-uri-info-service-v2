package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendField;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendOption;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import mu.semte.ch.uriinfo.v2.app.service.CrudTripleService;
import mu.semte.ch.uriinfo.v2.app.service.UIBuilderService;
import mu.semte.ch.uriinfo.v2.app.service.UriInfoService;
import mu.semte.ch.lib.utils.ModelUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Optional.ofNullable;

@RestController
@Slf4j
public class AppController {
    private final UriInfoService uriInfoService;
    private final UIBuilderService uiBuilderService;
    private final CrudTripleService crudTripleService;

    public AppController(UriInfoService uriInfoService,
                         UIBuilderService uiBuilderService,
                         CrudTripleService crudTripleService) {
        this.uriInfoService = uriInfoService;
        this.uiBuilderService = uiBuilderService;
        this.crudTripleService = crudTripleService;
    }

    @GetMapping(value = "/model",
            produces = "application/ld+json")
    public ResponseEntity<String> model(@RequestParam("uri") String uri) {
        Model model = uriInfoService.fetchSubject(uri);
        String response = ModelUtils.toString(model, Lang.JSONLD);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/meta",
            produces = "application/ld+json")
    public ResponseEntity<String> metaModel(@RequestParam("uri") String uri) {
        var metaModel = uriInfoService.fetchPage(uri);
        String response = ModelUtils.toString(metaModel, Lang.JSONLD);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/page")
    public ResponseEntity<FrontendUI> page(@RequestParam("uri") String uri) {
        try {
            FrontendUI ui = uiBuilderService.build(uri);
            if (ofNullable(ui).map(FrontendUI::getPages).orElseGet(List::of).isEmpty()) {
                log.info("No content for uri {}", uri);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ui);
        } catch (Throwable throwable) {
            log.error("An error occurred", throwable);
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<List<FrontendStmt>> updateTriples(@RequestBody List<FrontendStmt> triples) {
        return ResponseEntity.ok(crudTripleService.updateTriples(triples));
    }

    @PostMapping("/list-options")
    public ResponseEntity<List<FrontendOption>> listOptions(@RequestBody FrontendField field) {
        return ResponseEntity.ok(crudTripleService.listOptions(field));
    }
}
