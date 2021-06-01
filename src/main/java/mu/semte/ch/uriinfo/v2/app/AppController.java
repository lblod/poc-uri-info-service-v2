package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendForm;
import mu.semte.ch.uriinfo.v2.app.service.UIBuilderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AppController {
  private final UIBuilderService uiBuilderService;

  public AppController(UIBuilderService uiBuilderService) {
    this.uiBuilderService = uiBuilderService;
  }

  /*   @GetMapping(value = "/model",
             produces = CONTENT_TYPE_JSON_LD)
     public ResponseEntity<String> model(@RequestParam("uri") String uri) {
         return ResponseEntity.badRequest().build();
     }

     @GetMapping(value = "/meta",
             produces = CONTENT_TYPE_JSON_LD)
     public ResponseEntity<String> metaModel(@RequestParam("uri") String uri) {
         return ResponseEntity.badRequest().build();
     }

     @GetMapping("/page")
     public ResponseEntity<FrontendUI> page(@RequestParam("uri") String uri) {
         return ResponseEntity.badRequest().build();
     }*/
  @GetMapping("/page")
  public ResponseEntity<FrontendUI> page(@RequestParam(value = "uri") String uri,
                                         @RequestParam(value = "page",
                                                       required = false) String pageUri) {

    FrontendUI ui = uiBuilderService.build(uri, pageUri);
    return ResponseEntity.ok(ui);
  }
  @GetMapping("/form")
  public ResponseEntity<FrontendForm> form(@RequestParam(value = "uri") String uri,
                                         @RequestParam(value = "form") String formUri) {

    FrontendForm ui = uiBuilderService.buildForm(uri, formUri);
    return ResponseEntity.ok(ui);
  }
}
