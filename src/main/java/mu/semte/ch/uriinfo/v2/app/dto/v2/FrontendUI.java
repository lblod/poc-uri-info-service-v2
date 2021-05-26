package mu.semte.ch.uriinfo.v2.app.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
public class FrontendUI {
    private String uri;
    private List<FrontendMenuLink> menu;
    private FrontendPage page;

}
