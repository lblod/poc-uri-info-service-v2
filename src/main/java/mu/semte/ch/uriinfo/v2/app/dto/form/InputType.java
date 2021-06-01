package mu.semte.ch.uriinfo.v2.app.dto.form;

import org.apache.jena.rdf.model.Resource;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_INPUT_TEXT;

public enum InputType {
  TEXT, SELECT, MULTI_SELECT, DATE, TIME, NUMBER;

  public static InputType evaluateType(Resource resource) {
    if (C_INPUT_TEXT.equals(resource)) {
      return InputType.TEXT;
    }
    throw new RuntimeException("%s not yet supported".formatted(resource.getURI()));
  }
}
