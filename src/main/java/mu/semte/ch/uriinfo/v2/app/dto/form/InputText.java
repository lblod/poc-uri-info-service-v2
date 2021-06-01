package mu.semte.ch.uriinfo.v2.app.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class InputText implements Input{
  private int ordering;
  private String label;
  private String predicateUri;
  private String value;
  private String metaUri;

  @Override
  public InputType getType() {
    return InputType.TEXT;
  }
}
