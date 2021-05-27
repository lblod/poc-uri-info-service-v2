package mu.semte.ch.uriinfo.v2.app.service;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.FrontendVoc;
import mu.semte.ch.uriinfo.v2.app.dto.ElementType;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendContainer;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendElement;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendField;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendMenuLink;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendPage;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendPanel;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.*;


@Service
@Slf4j
public class UIBuilderService {
  private final InMemoryTripleStoreService inMemoryTripleStoreService;
  private final UriInfoService uriInfoService;
  private final Slugify slugify;

  public UIBuilderService(InMemoryTripleStoreService inMemoryTripleStoreService,
                          UriInfoService uriInfoService,
                          Slugify slugify) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
    this.uriInfoService = uriInfoService;
    this.slugify = slugify;
  }

  public FrontendUI build(String uri, String currentPageMetaUri) {
    FrontendUI ui = new FrontendUI();
    String typeUri = uriInfoService.fetchType(uri);
    Model metaModel = inMemoryTripleStoreService.getNamedModel(typeUri);
    ui.setMenu(this.buildMenu(metaModel, currentPageMetaUri));
    ui.setUri(uri);
    ui.setPage(this.buildPage(metaModel, uri, typeUri, ui.getMenu()
                                                         .stream()
                                                         .filter(FrontendMenuLink::isActive)
                                                         .map(FrontendMenuLink::getUri)
                                                         .findFirst()
                                                         .orElseThrow()));
    return ui;
  }

  protected FrontendPage buildPage(Model metaModel, String uri, String typeUri, String currentPageMetaUri) {
    FrontendPage page = new FrontendPage();
    page.setOrdering(metaModel.getProperty(ResourceFactory.createProperty(currentPageMetaUri), P_ORDERING).getInt());
    page.setTitle(this.buildTitle(metaModel, uri, typeUri, currentPageMetaUri, P_TITLE));
    page.setSubtitle(this.buildTitle(metaModel, uri, typeUri, currentPageMetaUri, P_SUB_TITLE));
    page.setContainers(this.buildContainers(metaModel, uri, typeUri, currentPageMetaUri));
    return page;
  }

  private List<FrontendContainer> buildContainers(Model metaModel, String uri, String typeUri, String currentPageMetaUri) {
    Property pageProp = ResourceFactory.createProperty(currentPageMetaUri);
    var containersProp = metaModel.getRequiredProperty(pageProp, P_CONTAINERS);
    var containerParts = metaModel.getList(containersProp.getObject().asResource()).asJavaList();
    return containerParts.stream()
                         .map(RDFNode::asResource)
                         .map(containerPart -> {
                           FrontendContainer container = new FrontendContainer();
                           container.setTitle(this.buildTitle(metaModel, uri, typeUri, containerPart.getURI(), P_TITLE));
                           container.setOrdering(metaModel.getProperty(containerPart, P_ORDERING).getInt());
                           container.setElements(this.buildElements(metaModel, uri, typeUri, currentPageMetaUri, containerPart));
                           return container;
                         }).collect(Collectors.toList());
  }

  private List<FrontendElement> buildElements(Model metaModel,
                                              String uri,
                                              String typeUri,
                                              String currentPageMetaUri,
                                              Resource containerPart) {
    // todo Property pageProp = ResourceFactory.createProperty(currentPageMetaUri);
    var elementsProp = metaModel.getRequiredProperty(containerPart, P_ELEMENTS);
    var elementParts = metaModel.getList(elementsProp.getObject().asResource()).asJavaList();
    return elementParts.stream()
                         .map(RDFNode::asResource)
                          .map(elementPart ->{
                            Statement elementTypeStmt = metaModel.getRequiredProperty(elementPart, RDF.type);
                            ElementType elementType = ElementType.evaluateType(elementTypeStmt.getResource());
                            FrontendElement element = switch (elementType){
                              case PANEL -> this.buildPanel(metaModel, uri, typeUri, currentPageMetaUri, elementPart);
                              case TABLE -> null;
                            };
                            return element;
                          }).collect(Collectors.toList());
  }

  private FrontendElement buildPanel(Model metaModel, String uri, String typeUri, String currentPageMetaUri, Resource elementPart) {
    FrontendPanel panel = new FrontendPanel();
    panel.setType(ElementType.PANEL);
    panel.setOrdering(metaModel.getProperty(elementPart, P_ORDERING).getInt());
    panel.setEditable(ofNullable(metaModel.getProperty(elementPart, P_EDITABLE)).map(Statement::getBoolean).orElse(false));
    var fieldsProp = metaModel.getRequiredProperty(elementPart, P_FIELDS);
    var fieldParts = metaModel.getList(fieldsProp.getObject().asResource()).asJavaList();
    panel.setFields(fieldParts.stream()
                              .map(RDFNode::asResource)
                              .map(fieldPart -> {
                                FrontendField field = new FrontendField();
                                field.setOrdering(metaModel.getProperty(fieldPart, P_ORDERING).getInt());
                                field.setType(FrontendField.FrontendFieldType.valueOf(metaModel.getProperty(fieldPart, P_FIELD_TYPE).getString()));
                                field.setLabel(metaModel.getProperty(fieldPart, P_LABEL).getString());
                                field.setValue(this.queryForField(fieldPart,metaModel,uri, typeUri));
                                // todo add link
                                return field;
                              }).collect(Collectors.toList()));
    return panel;
  }


  protected String buildTitle(Model metaModel, String uri, String typeUri, String resourceUri, Property titleProperty) {
    Property resourceProp = ResourceFactory.createProperty(resourceUri);
    var title = metaModel.getRequiredProperty(resourceProp, titleProperty);
    String separator = ofNullable(metaModel.getProperty(resourceProp, P_SEPARATOR)).map(Statement::getString).orElse(" ");
    RDFList titleParts = metaModel.getList(title.getObject().asResource());
    return titleParts.asJavaList().stream().map(titlePart -> {
      if (titlePart.isLiteral()) {
        return titlePart.asLiteral().getString();
      }
      Resource titleField = titlePart.asResource();
      return this.queryForField(titleField, metaModel, uri, typeUri);
    }).collect(Collectors.joining(separator));
  }


  private String queryForField(Resource fieldResource, Model metaModel, String uri, String typeUri) {
    //todo just make it simple stupid for now
    Statement fieldsProperty = metaModel.getRequiredProperty(fieldResource, P_FIELDS);
    String separator = ofNullable(metaModel.getProperty(fieldResource, P_SEPARATOR)).map(Statement::getString).orElse(" ");

    RDFList innerFields = metaModel.getList(fieldsProperty.getObject().asResource());
    Map<String, String> variableQuery = innerFields.asJavaList().stream().map(RDFNode::asResource)
                                                   .map(Resource::getURI)
                                                   .map(iri -> Map.entry(SplitIRI.localname(iri), iri))
                                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s, s2) -> s, LinkedHashMap::new));

    var result = this.uriInfoService.dynamicQuery(uri, typeUri, variableQuery);

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

  private List<RDFNode> getPages(Model metaModel) {
    var pages = metaModel.getRequiredProperty(null, FrontendVoc.P_PAGES);
    RDFList list = metaModel.getList(pages.getObject().asResource());
    return list.asJavaList();
  }

}
