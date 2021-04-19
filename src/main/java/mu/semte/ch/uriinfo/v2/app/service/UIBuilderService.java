package mu.semte.ch.uriinfo.v2.app.service;

import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendElement;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendField;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendPage;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendRow;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendSidePanel;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendStmt;
import mu.semte.ch.uriinfo.v2.app.dto.FrontendUI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_FIELD;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.C_MULTI_LEVEL_FIELD;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_DETAIL_PANEL;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_EDITABLE;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_ELEMENTS;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_FIELDS;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_FIELD_TYPE;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_LABEL;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_META_FIELD;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_ORDERING;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_PAGES;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SEPARATOR;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SIDE_PANEL_TITLE;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SOURCE;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SUBJECTS;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_TITLE;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_TYPE;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

@Service
@Slf4j
public class UIBuilderService {
  private final UriInfoService uriInfoService;
  private final Slugify slugify;

  public UIBuilderService(UriInfoService uriInfoService, Slugify slugify) {
    this.uriInfoService = uriInfoService;
    this.slugify = slugify;
  }

  public FrontendUI build(String uri) {
    var metaModel = uriInfoService.fetchPage(uri);
    var model = uriInfoService.fetchSubject(uri);
    var pages = metaModel.listObjectsOfProperty(P_PAGES).toList().stream()
                         .map(RDFNode::asResource)
                         .map(page -> this.buildPage(uri, model, metaModel, page))
                         .collect(Collectors.toList());


    var ui = new FrontendUI();
    ui.setUri(uri);
    ui.setPages(pages);
    return ui;
  }


  private FrontendPage buildPage(String uri, Model model, Model metaModel, Resource page) {
    FrontendPage fp = new FrontendPage();
    String title = metaModel.getProperty(page, P_TITLE).getString();
    fp.setTitle(title);
    fp.setSlug(slugify.slugify(title));
    fp.setOrdering(metaModel.getProperty(page, P_ORDERING).getInt());
    fp.setElements(metaModel.listObjectsOfProperty(page, P_ELEMENTS).toList().stream()
                            .map(RDFNode::asResource)
                            .map(element -> this.buildElement(createResource(uri), model, metaModel, element))
                            .sorted()
                            .collect(Collectors.toList()));
    return fp;
  }

  private FrontendElement buildElement(Resource uri, Model model, Model metaModel, Resource element) {
    FrontendElement el = new FrontendElement();
    el.setTitle(ofNullable(metaModel.getProperty(element, P_TITLE)).map(Statement::getString).orElse(null));
    el.setOrdering(metaModel.getProperty(element, P_ORDERING).getInt());
    el.setType(FrontendElement.evaluateType(metaModel.getProperty(element, P_TYPE).getObject().asResource()));
    el.setEditable(metaModel.getProperty(element, P_EDITABLE).getBoolean());
    List<Resource> fields = metaModel.listObjectsOfProperty(element, P_FIELDS).toList().stream()
                                     .map(RDFNode::asResource)
                                     .collect(Collectors.toList());

    if (FrontendElement.ElementType.TABLE.equals(el.getType())) {
      var rows = model.listObjectsOfProperty(uri, createProperty(metaModel.getProperty(element, P_SOURCE)
                                                                          .getResource()
                                                                          .getURI())).toList()
                      .stream().map(RDFNode::asResource)
                      .map(iri -> this.buildRow(iri, model, metaModel, element, fields))
                      .collect(Collectors.toList());
      el.setRows(rows);
    }
    else if (FrontendElement.ElementType.PANEL.equals(el.getType())) {
      el.setFields(fields.stream().map(f -> this.buildField(uri, model, metaModel, f)).sorted().collect(Collectors.toList()));
    }


    return el;
  }

  private FrontendRow buildRow(Resource uri, Model model, Model metaModel, Resource element, List<Resource> fields) {
    FrontendRow row = new FrontendRow();
    row.setFields(fields.stream().map(f -> this.buildField(uri, model, metaModel, f)).sorted().collect(Collectors.toList()));
    ofNullable(metaModel.getProperty(element, P_DETAIL_PANEL)).ifPresent(dp -> {
      FrontendSidePanel detailPanel = new FrontendSidePanel();
      Resource detailPanelSubject = dp.getResource();
      detailPanel.setElements(metaModel.listObjectsOfProperty(detailPanelSubject, P_ELEMENTS).toList().stream()
                                       .map(RDFNode::asResource)
                                       .map(el -> this.buildElement(uri, model, metaModel, el))
                                       .sorted()
                                       .collect(Collectors.toList()));
      detailPanel.setLabel(ofNullable(metaModel.getProperty(detailPanelSubject, P_LABEL)).map(Statement::getString)
                                                                                         .orElse("Missing Label"));
      detailPanel.setTitle(ofNullable(metaModel.getProperty(detailPanelSubject, P_SIDE_PANEL_TITLE))
                                   .map(Statement::getResource)
                                   .flatMap(titleSubject -> ofNullable(metaModel.getProperty(titleSubject, P_META_FIELD)))
                                   .map(Statement::getResource)
                                   .map(metaField -> this.buildField(uri, model, metaModel, metaField))
                                   .map(FrontendField::getValue)
                                   .orElse(detailPanel.getLabel()));

      row.setDetailPanel(detailPanel);
    });

    return row;
  }

  private FrontendField buildField(Resource rootResource, Model model, Model metaModel, Resource field) {
    FrontendField f = new FrontendField();
    String separator = ofNullable(metaModel.getProperty(field, P_SEPARATOR)).map(Statement::getString).orElse(" ");
    Resource nsType = metaModel.getProperty(field, RDF.type).getObject().asResource();
    var fields = metaModel.listObjectsOfProperty(field, P_FIELDS).toList().stream()
                          .map(RDFNode::asResource).collect(Collectors.toList());
    f.setLabel(ofNullable(metaModel.getProperty(field, P_LABEL)).map(Statement::getString).orElse(null));
    f.setOrdering(ofNullable(metaModel.getProperty(field, P_ORDERING)).map(Statement::getInt).orElse(0));
    f.setType(ofNullable(metaModel.getProperty(field, P_FIELD_TYPE)).map(Statement::getString).orElse(null));
    f.setLabelUris(fields.stream().map(Resource::getURI).collect(Collectors.toList()));
    if (C_FIELD.equals(nsType)) {
      String value = fields.stream()
                           .map(fieldPropertyUri -> model.getProperty(rootResource, createProperty(fieldPropertyUri.getURI()))
                                                         .getString())
                           .collect(Collectors.joining(separator));
      f.setValue(value);
      f.setTriples(this.buildTriples(model, rootResource, fields));
    }
    else if (C_MULTI_LEVEL_FIELD.equals(nsType)) {
      var source = model.getProperty(rootResource, createProperty(metaModel.getProperty(field, P_SOURCE)
                                                                           .getResource()
                                                                           .getURI())).getObject().asResource();
      buildMultiLevelField(f, model, metaModel, source, field, separator, fields);
    }
    return f;
  }

  private List<FrontendStmt> buildTriples(Model model, Resource rootResource, List<Resource> fields) {
    return fields.stream().map(fieldPropertyUri -> {
      Statement property = model.getProperty(rootResource, createProperty(fieldPropertyUri.getURI()));
      return FrontendStmt.builder()
                         .subject(property.getSubject().getURI())
                         .predicate(fieldPropertyUri.getURI())
                         .object(property.getLiteral().getString())
                         .datatype(property.getLiteral().getDatatypeURI())
                         .language(property.getLiteral().getLanguage())
                         .build();
    }).collect(Collectors.toList());
  }

  private void buildMultiLevelField(FrontendField frontendField,
                                    Model model,
                                    Model metaModel,
                                    Resource source,
                                    Resource field,
                                    String separator,
                                    List<Resource> fields) {
    List<Resource> predicates = metaModel.listObjectsOfProperty(field, P_SUBJECTS)
                                         .toList()
                                         .stream()
                                         .map(RDFNode::asResource)
                                         .collect(Collectors.toList());
    String value;
    List<FrontendStmt> triples = null;
    if (predicates.size() >= 1) {
      var lastDepth = findDepth(model, source, predicates);
      value = fields.stream()
                    .map(fieldPropertyUri -> lastDepth.getProperty(null, createProperty(fieldPropertyUri.getURI())).getString())
                    .collect(Collectors.joining(separator));
      triples = this.buildTriples(lastDepth, null, fields);

    }
    else {
      value = fields.stream()
                    .map(fieldPropertyUri -> model.getProperty(source, createProperty(fieldPropertyUri.getURI())).getString())
                    .collect(Collectors.joining(separator));
      triples = this.buildTriples(model, source, fields);
    }
    frontendField.setValue(value);
    frontendField.setTriples(triples);
  }

  private Model findDepth(Model model, Resource subject, List<Resource> predicates) {
    var root = model.listStatements(subject, null, (RDFNode) null);
    if (predicates.isEmpty()) {
      return root.toModel();
    }
    var rootSource = root.toList();
    Resource rootPredicate = predicates.stream()
                                       .filter(s -> rootSource.stream()
                                                              .anyMatch(rs -> rs.getPredicate().asResource().equals(s)))
                                       .findFirst()
                                       .get();
    var newSubject = rootSource.stream()
                               .filter(rs -> rs.getPredicate().equals(rootPredicate))
                               .findFirst()
                               .get()
                               .getObject()
                               .asResource();
    return findDepth(model, newSubject, predicates.stream().filter(p -> !p.equals(rootPredicate)).collect(Collectors.toList()));
  }

}
