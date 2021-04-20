package mu.semte.ch.uriinfo.v2.app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_PANEL;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_TABLE;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendElement implements Comparable<FrontendElement> {

  @Override
  public int compareTo(FrontendElement o) {
    return Integer.compare(this.ordering, o.ordering);
  }

  public enum ElementType {TABLE, PANEL}

  private String title;
  private ElementType type;
  private boolean editable;
  private int ordering;
  private List<FrontendField> fields;
  private List<FrontendRow> rows;
  private List<FrontendField> metaFields = new ArrayList<>(); // skeleton of the element

  public static ElementType evaluateType(Resource resource) {
    if (C_TABLE.equals(resource)) {
      return ElementType.TABLE;
    }
    else if (C_PANEL.equals(resource)) {
      return ElementType.PANEL;
    }
    return null;
  }

}
