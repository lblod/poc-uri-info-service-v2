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
public class FrontendField implements Comparable<FrontendField> {
    private int ordering;
    private List<String> labelUris;
    private String type;
    private String label;
    private String value;
    private List<FrontendStmt> triples;
    private String link;

    @Override
    public int compareTo(FrontendField o) {
        return Integer.compare(this.ordering, o.ordering);
    }
}
