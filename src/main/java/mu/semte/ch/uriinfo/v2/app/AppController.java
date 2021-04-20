package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import mu.semte.ch.uriinfo.v2.app.service.UIBuilderService;
import mu.semte.ch.uriinfo.v2.app.service.CrudTripleService;
import mu.semte.ch.uriinfo.v2.app.service.UriInfoService;
import mu.semte.ch.uriinfo.v2.lib.utils.ModelUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
  public ResponseEntity<String> model(@RequestParam("uri") String uri, HttpServletRequest request) {
    Model model = uriInfoService.fetchSubject(uri);
    String response = ModelUtils.toString(model, Lang.JSONLD);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/meta",
              produces = "application/ld+json")
  public ResponseEntity<String> metaModel(@RequestParam("uri") String uri, HttpServletRequest request) {
    var metaModel = uriInfoService.fetchPage(uri);
    String response = ModelUtils.toString(metaModel, Lang.JSONLD);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/page")
  public ResponseEntity<FrontendUI> page(@RequestParam("uri") String uri, HttpServletRequest request) {
    return ResponseEntity.ok(uiBuilderService.build(uri));
  }

  @PostMapping("/update")
  public ResponseEntity<List<FrontendStmt>> updateTriples(@RequestBody List<FrontendStmt> triples, HttpServletRequest request) {
    return ResponseEntity.ok(crudTripleService.updateTriples(triples));
  }
}
