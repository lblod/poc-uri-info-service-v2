package mu.semte.ch.uriinfo.v2.lib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {
  private String type;
  private String value;
  private String datatype;
}
