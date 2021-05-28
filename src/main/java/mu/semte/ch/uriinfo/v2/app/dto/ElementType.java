package mu.semte.ch.uriinfo.v2.app.dto;

import org.apache.jena.rdf.model.Resource;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_CUSTOM;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_PANEL;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_TABLE;

public enum ElementType {
  TABLE, PANEL, CUSTOM;

  public static ElementType evaluateType(Resource resource) {
    if (C_TABLE.equals(resource)) {
      return ElementType.TABLE;
    }
    else if (C_PANEL.equals(resource)) {
      return ElementType.PANEL;
    }
    else if (C_CUSTOM.equals(resource)) {
      return ElementType.CUSTOM;
    }
    return null;
  }
}
