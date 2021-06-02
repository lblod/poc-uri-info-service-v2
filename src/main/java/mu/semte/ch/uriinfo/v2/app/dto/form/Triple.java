package mu.semte.ch.uriinfo.v2.app.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Triple {
  private String subject;
  private String predicate;
  private String object;
}
