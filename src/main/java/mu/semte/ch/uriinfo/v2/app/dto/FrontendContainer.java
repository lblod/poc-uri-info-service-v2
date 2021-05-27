package mu.semte.ch.uriinfo.v2.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Resource;


import java.util.List;

import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_PANEL;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_TABLE;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendContainer implements Comparable<FrontendContainer>{
    public enum ContainerType {TABLE, PANEL}
    private String title;
    private ContainerType type;
    private int ordering;
    private List<FrontendElement> elements;

    @Override
    public int compareTo(FrontendContainer o) {
        return Integer.compare(this.ordering, o.ordering);
    }

    public static ContainerType evaluateType(Resource resource) {
        if (C_TABLE.equals(resource)) {
            return ContainerType.TABLE;
        } else if (C_PANEL.equals(resource)) {
            return ContainerType.PANEL;
        }
        return null;
    }
}
