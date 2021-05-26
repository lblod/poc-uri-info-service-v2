package mu.semte.ch.uriinfo.v2.app.dto.v2;

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
    private boolean active;
}
