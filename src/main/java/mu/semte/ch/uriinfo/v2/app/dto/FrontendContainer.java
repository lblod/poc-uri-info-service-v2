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
public class FrontendContainer implements Comparable<FrontendContainer> {

  private String title;
  private int ordering;
  private List<FrontendElement> elements;

  @Override
  public int compareTo(FrontendContainer o) {
    return Integer.compare(this.ordering, o.ordering);
  }


}
