package mu.semte.ch.uriinfo.v2.app.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendPage {
    private String title;
    private String subtitle;
    private int ordering;
}
