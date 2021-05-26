package mu.semte.ch.uriinfo.v2.app.service;

import com.github.slugify.Slugify;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.v2.*;
import org.apache.jena.rdf.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;


@Service
public class UIBuilderServiceV2 {
    private final InMemoryTripleStoreService inMemoryTripleStoreService;
    private final UriInfoServiceV2 uriInfoServiceV2;
    private final Slugify slugify;

    public UIBuilderServiceV2(InMemoryTripleStoreService inMemoryTripleStoreService, UriInfoServiceV2 uriInfoServiceV2, Slugify slugify) {
        this.inMemoryTripleStoreService = inMemoryTripleStoreService;
        this.uriInfoServiceV2 = uriInfoServiceV2;
        this.slugify = slugify;
    }

    public FrontendUI build(String uri, String currentPageMetaUri) {
        FrontendUI ui = new FrontendUI();
        String typeUri = uriInfoServiceV2.fetchType(uri);
        Model metaModel = inMemoryTripleStoreService.getNamedModel(typeUri);
        ui.setMenu(this.buildMenu(metaModel, currentPageMetaUri));
        ui.setUri(uri);

        return ui;
    }

    protected FrontendPage generatePage(Model metaModel, String pageUri){
        return null;
    }

    protected List<FrontendMenuLink> buildMenu(Model metaModel, String pageUri) {
        List<RDFNode> pages = getPages(metaModel);
        return pages.stream()
                .map(RDFNode::asResource)
                .map(Resource::getURI)
                .map(uri -> {
                    var label = ofNullable(metaModel.getProperty(ResourceFactory.createProperty(uri), FrontendVoc.P_LABEL))
                            .map(Statement::getLiteral)
                            .map(Literal::getString)
                            .orElse(null);
                    return FrontendMenuLink.builder()
                                            .slug(ofNullable(label).map(slugify::slugify).orElse(null))
                                            .label(label)
                                            .uri(uri)
                                            .active(uri.equals(pageUri)).build();
                })
                .collect(Collectors.toList());
    }

    private List<RDFNode> getPages(Model metaModel){
        var pages = metaModel.getRequiredProperty(null, FrontendVoc.P_PAGES);
        RDFList list = metaModel.getList(pages.getObject().asResource());
        return list.asJavaList();
    }

}
