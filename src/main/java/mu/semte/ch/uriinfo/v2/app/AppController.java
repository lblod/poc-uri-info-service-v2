package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendElement;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendForm;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendFormRequest;
import mu.semte.ch.uriinfo.v2.app.service.FormService;
import mu.semte.ch.uriinfo.v2.app.service.UIBuilderService;
import mu.semte.ch.uriinfo.v2.app.service.UIFormService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AppController {
  private final UIBuilderService uiBuilderService;
  private final UIFormService uiFormService;
  private final FormService formService;

  public AppController(UIBuilderService uiBuilderService,
                       UIFormService uiFormService,
                       FormService formService) {
    this.uiBuilderService = uiBuilderService;
    this.uiFormService = uiFormService;
    this.formService = formService;
  }

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

    FrontendForm ui = uiFormService.buildForm(uri, formUri);
    return ResponseEntity.ok(ui);
  }

  @PostMapping("/form")
  public ResponseEntity<FrontendElement> formPost(@RequestBody FrontendFormRequest request){
    String elementUpdatedUri = formService.persist(request);
    return ResponseEntity.ok(uiBuilderService.buildSelectedElement(request.getUri(), elementUpdatedUri));
  }
}
