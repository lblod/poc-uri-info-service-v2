package mu.semte.ch.uriinfo.v2.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendField implements Comparable<FrontendField> {
    public enum FrontendFieldType {
        STRING, NUMBER, BADGE, BOOLEAN
    }

    private int ordering;
    private FrontendFieldType type;
    private String label;
    private String value;
    private String link;

    @Override
    public int compareTo(FrontendField o) {
        return Integer.compare(this.ordering, o.ordering);
    }
}
