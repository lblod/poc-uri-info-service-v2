package mu.semte.ch.uriinfo.v2.app.dto.form;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendFormRequest {
  private String uri;
  private String formUri;
  private String typeUri;
  private ObjectNode skeleton;
}
