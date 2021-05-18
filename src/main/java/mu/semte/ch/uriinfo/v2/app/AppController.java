package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import mu.semte.ch.uriinfo.v2.app.service.UIBuilderService;
import mu.semte.ch.uriinfo.v2.app.service.UriInfoService;
import mu.semte.ch.lib.utils.ModelUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.lib.Constants.CONTENT_TYPE_JSON_LD;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_FIELDS;

@RestController
@Slf4j
public class AppController {
    private final UriInfoService uriInfoService;
    private final UIBuilderService uiBuilderService;

    public AppController(UriInfoService uriInfoService,
                         UIBuilderService uiBuilderService) {
        this.uriInfoService = uriInfoService;
        this.uiBuilderService = uiBuilderService;
    }

    @GetMapping(value = "/model",
            produces = CONTENT_TYPE_JSON_LD)
    public ResponseEntity<String> model(@RequestParam("uri") String uri) {
        Model model = uriInfoService.fetchSubject(uri);
        String response = ModelUtils.toString(model, Lang.JSONLD);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/meta",
            produces = CONTENT_TYPE_JSON_LD)
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

}
