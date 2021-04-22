package mu.semte.ch.uriinfo.v2.lib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Triple {
    private Node subject;
    private Node predicate;
    private Node object;
}
