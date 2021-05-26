package mu.semte.ch.uriinfo.v2.app.service;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.v2.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.SplitIRI;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.*;


@Service
@Slf4j
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
        log.error(this.buildTitle(metaModel, uri, typeUri, ui.getMenu().stream().filter(FrontendMenuLink::isActive).map(FrontendMenuLink::getUri).findFirst().orElseThrow()));
        return ui;
    }

    protected FrontendPage generatePage(Model metaModel, String uri, String currentPageMetaUri){
        return null;
    }


    protected String buildTitle(Model metaModel, String uri, String typeUri, String currentPageMetaUri) {
        Property pageProp = ResourceFactory.createProperty(currentPageMetaUri);
        var title = metaModel.getRequiredProperty(pageProp, P_TITLE);
        String separator = ofNullable(metaModel.getProperty(pageProp, P_SEPARATOR)).map(Statement::getString).orElse(" ");

        RDFList list = metaModel.getList(title.getObject().asResource());
        return list.asJavaList().stream().map(titlePart-> {
            if(titlePart.isLiteral()){
                return titlePart.asLiteral().getString();
            }
            Resource titleField = titlePart.asResource();
            return this.buildField(titleField,metaModel,uri,typeUri);
        }).collect(Collectors.joining(separator));
    }


    private String buildField(Resource fieldResource, Model metaModel, String uri, String typeUri) {
        //todo just make it simple stupid for now
        Statement fieldsProperty = metaModel.getRequiredProperty(fieldResource, P_FIELDS);
        String separator = ofNullable(metaModel.getProperty(fieldResource, P_SEPARATOR)).map(Statement::getString).orElse(" ");

        RDFList innerFields = metaModel.getList(fieldsProperty.getObject().asResource());
        Map<String, String> variableQuery = innerFields.asJavaList().stream().map(RDFNode::asResource)
                .map(Resource::getURI)
                .map(iri -> Map.entry(SplitIRI.localname(iri), iri))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s, s2) -> s, LinkedHashMap::new));

        var result = this.uriInfoServiceV2.dynamicQuery(uri,typeUri, variableQuery);

         return result.stream().findFirst().map(m -> String.join(separator, m.values())).orElse(null);
    }






    protected List<FrontendMenuLink> buildMenu(Model metaModel, String pageUri) {
        List<RDFNode> pages = getPages(metaModel);
        String mainPageUri = metaModel.getRequiredProperty(null, FrontendVoc.P_MAIN_PAGE).getResource().getURI();
        return pages.stream()
                .map(RDFNode::asResource)
                .map(Resource::getURI)
                .map(uri -> {
                    var label = ofNullable(metaModel.getProperty(ResourceFactory.createProperty(uri), FrontendVoc.P_LABEL))
                            .map(Statement::getLiteral)
                            .map(Literal::getString)
                            .orElse(null);
                    boolean mainPage = mainPageUri.equals(uri);
                    return FrontendMenuLink.builder()
                                            .slug(ofNullable(label).map(slugify::slugify).orElse(null))
                                            .label(label)
                                            .uri(uri)
                                            .mainPage(mainPage)
                                            .active(StringUtils.isNotEmpty(pageUri) ? uri.equals(pageUri) : mainPage).build();
                })
                .collect(Collectors.toList());
    }

    private List<RDFNode> getPages(Model metaModel){
        var pages = metaModel.getRequiredProperty(null, FrontendVoc.P_PAGES);
        RDFList list = metaModel.getList(pages.getObject().asResource());
        return list.asJavaList();
    }

}
