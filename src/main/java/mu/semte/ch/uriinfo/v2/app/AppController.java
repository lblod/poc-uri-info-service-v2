package mu.semte.ch.uriinfo.v2.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static mu.semte.ch.lib.Constants.CONTENT_TYPE_JSON_LD;

@RestController
@Slf4j
public class AppController {

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

}
