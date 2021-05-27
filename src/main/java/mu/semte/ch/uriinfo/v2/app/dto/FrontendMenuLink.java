package mu.semte.ch.uriinfo.v2.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendMenuLink {
  private String label;
  private String uri;
  private String slug;
  private boolean mainPage;
  private boolean active;
}
