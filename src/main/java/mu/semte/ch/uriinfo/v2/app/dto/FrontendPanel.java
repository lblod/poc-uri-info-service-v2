package mu.semte.ch.uriinfo.v2.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendPanel implements FrontendElement {
  private boolean editable;
  private String editFormUri;
  private int ordering;
  private List<FrontendField> fields;

  @Override
  public ElementType getType() {
    return ElementType.PANEL;
  }
}
