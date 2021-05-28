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
public class FrontendTable implements FrontendElement  {
  private boolean editable;
  private int ordering;
  private List<FrontendTableRow> rows;

  @Override
  public ElementType getType() {
    return ElementType.TABLE;
  }
  // private List<FrontendField> fields;
}
