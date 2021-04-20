package mu.semte.ch.uriinfo.v2.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendStmt {
  private String subject;
  private String predicate;
  private String object;
  private String datatype;
  private String language;
  private String predicateLabel;
}
