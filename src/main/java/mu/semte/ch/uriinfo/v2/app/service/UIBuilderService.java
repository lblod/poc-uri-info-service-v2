package mu.semte.ch.uriinfo.v2.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.*;
import mu.semte.ch.lib.dto.JsonApiData;
import mu.semte.ch.lib.dto.JsonApiResponse;
import mu.semte.ch.lib.utils.ModelUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.*;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

@Service
@Slf4j
public class UIBuilderService {
    private final UriInfoService uriInfoService;
    private final Slugify slugify;

    @Value("${resource-labels.endpoint}")
    private String resourceLabelsEndpoint;
    @Value("${resource-labels.enabled}")
    private boolean resourceLabelsEnabled;

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

        el.setMetaFields(fields.stream().map(f -> this.buildSkeletonField(metaModel, f)).sorted().collect(Collectors.toList()));

        if (FrontendElement.ElementType.TABLE.equals(el.getType())) {
            var rows = model.listObjectsOfProperty(uri, createProperty(metaModel.getProperty(element, P_SOURCE)
                    .getResource()
                    .getURI())).toList()
                    .stream().map(RDFNode::asResource)
                    .map(iri -> this.buildRow(iri, model, metaModel, element, fields))
                    .collect(Collectors.toList());
            el.setRows(rows);
        } else if (FrontendElement.ElementType.PANEL.equals(el.getType())) {
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
        FrontendField f = buildSkeletonField(metaModel, field);
        f.setLink(this.buildLink(rootResource, model, metaModel, field));
        var fields = metaModel.listObjectsOfProperty(field, P_FIELDS).toList().stream()
                .map(RDFNode::asResource).collect(Collectors.toList());
        f.setLabelUris(fields.stream().map(Resource::getURI).collect(Collectors.toList()));

        String separator = ofNullable(metaModel.getProperty(field, P_SEPARATOR)).map(Statement::getString).orElse(" ");
        Resource nsType = metaModel.getProperty(field, RDF.type).getObject().asResource();

        if (C_FIELD.equals(nsType)) {
            String value = fields.stream()
                    .map(fieldPropertyUri -> ofNullable(model.getProperty(rootResource, createProperty(fieldPropertyUri.getURI()))).map(Statement::getString).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(joining(separator));
            f.setValue(value);
            f.setTriples(this.buildTriples(model, rootResource, fields));
        } else if (C_MULTI_LEVEL_FIELD.equals(nsType)) {
            var optionalSource = ofNullable(metaModel.getProperty(field, P_SOURCE))
                    .map(Statement::getResource)
                    .map(Resource::getURI)
                    .map(ResourceFactory::createProperty)
                    .flatMap(property -> ofNullable(model.getProperty(rootResource, property)))
                    .map(Statement::getResource);
            optionalSource.ifPresent(resource -> buildMultiLevelField(f, model, metaModel, resource, field, separator, fields));
        }
        return f;
    }

    private String buildLink(Resource rootResource, Model model, Model metaModel, Resource field) {
        Resource hasLink = ofNullable(metaModel.getProperty(field, P_HAS_LINK)).map(Statement::getResource).orElse(null);
        if (hasLink != null) {
            var source = model.getProperty(rootResource, createProperty(metaModel.getProperty(field, P_SOURCE)
                    .getResource()
                    .getURI())).getObject().asResource();
            List<Resource> predicates = metaModel.listObjectsOfProperty(hasLink, P_SUBJECTS)
                    .toList()
                    .stream()
                    .map(RDFNode::asResource)
                    .collect(Collectors.toList());
            Model lastDepth = this.findDepth(model, source, predicates);
            if (!lastDepth.isEmpty()) {
                return lastDepth.listStatements().nextStatement().getSubject().getURI();
            }
        }
        return null;
    }

    private FrontendField buildSkeletonField(Model metaModel, Resource field) {
        FrontendField f = new FrontendField();
        f.setLabel(ofNullable(metaModel.getProperty(field, P_LABEL)).map(Statement::getString).orElse(null));
        f.setOrdering(ofNullable(metaModel.getProperty(field, P_ORDERING)).map(Statement::getInt).orElse(0));
        f.setType(ofNullable(metaModel.getProperty(field, P_FIELD_TYPE)).map(Statement::getString).orElse(null));
        return f;
    }

    private List<FrontendStmt> buildTriples(Model model, Resource rootResource, List<Resource> fields) {
        return fields.stream().map(fieldPropertyUri -> {
            Statement property = model.getProperty(rootResource, createProperty(fieldPropertyUri.getURI()));

            return FrontendStmt.builder()
                    .subject(ofNullable(property).map(Statement::getSubject).map(Resource::getURI).orElse(null))
                    .predicate(fieldPropertyUri.getURI())
                    .object(ofNullable(property).map(Statement::getLiteral).map(Literal::getString).orElse(null))
                    .datatype(ofNullable(property).map(Statement::getLiteral).map(Literal::getDatatypeURI).orElse(null))
                    .predicateLabel(this.fetchLabelFromResourceLabelsService(fieldPropertyUri.getURI()))
                    .language(ofNullable(property).map(Statement::getLiteral).map(Literal::getLanguage).orElse(null))

                    .build();
        }).collect(Collectors.toList());
    }

    private String fetchLabelFromResourceLabelsService(String uri) {
        if (resourceLabelsEnabled) {
            try {
                String endpoint = "%s/info?term=%s".formatted(resourceLabelsEndpoint, URLEncoder.encode(uri, StandardCharsets.UTF_8));
                URL url = new URL(endpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/vnd.api+json");
                InputStream responseStream = connection.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                var body = mapper.readValue(responseStream, JsonApiResponse.class);
                return ofNullable(body).map(JsonApiResponse::getData).map(JsonApiData::getAttributes).map(attributes -> attributes.getOrDefault("label", uri)).orElse(uri);
            } catch (Exception e) {
                log.debug("error while calling resource labels service", e);
            }

        }
        return uri;
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
        String value = null;
        List<FrontendStmt> triples = null;
        if (predicates.size() >= 1) {
            var lastDepth = findDepth(model, source, predicates);
            if(!lastDepth.isEmpty()){
                value = fields.stream()
                        .map(fieldPropertyUri -> lastDepth.getProperty(null, createProperty(fieldPropertyUri.getURI())))
                        .filter(Objects::nonNull)
                        .map(Statement::getString)
                        .collect(joining(separator));
                triples = this.buildTriples(lastDepth, null, fields);
            }

        } else {
            value = fields.stream()
                    .map(fieldPropertyUri -> model.getProperty(source, createProperty(fieldPropertyUri.getURI())))
                    .filter(Objects::nonNull)
                    .map(Statement::getString)
                    .collect(joining(separator));
            triples = this.buildTriples(model, source, fields);
        }
        frontendField.setValue(value);
        frontendField.setTriples(triples);
    }

    private Model findDepth(Model model, Resource subject, List<Resource> predicates) {

        if(model.isEmpty()) {
            log.error("model is empty");
            return model;
        }

        var root = model.listStatements(subject, null, (RDFNode) null);

        if (predicates.isEmpty()) {
            return root.toModel();
        }
        var rootSource = root.toList();
        Resource rootPredicate = predicates.stream()
                .filter(s -> rootSource.stream().anyMatch(rs -> rs.getPredicate().asResource().equals(s)))
                .findFirst().orElse(null);

        if(rootPredicate == null) {
            log.error("could not find predicate. rootSource:\n%s\nsubject:\n%s\npredicates:\n%s\n"
                    .formatted(ModelUtils.toString(root.toModel(), Lang.TURTLE), subject.getURI(), predicates.stream().map(Resource::getURI).collect(joining(","))));
            return createDefaultModel();
        }

        var newSubject = rootSource.stream()
                .filter(rs -> rs.getPredicate().equals(rootPredicate))
                .map(Statement::getObject)
                .findFirst()
                .map(RDFNode::asResource)
                .orElse(null);

        if(newSubject == null) {
            log.error("could not find subject. rootSource:\n%s\nrootPredicate:\n%s\npredicates:\n%s\n"
                    .formatted(ModelUtils.toString(root.toModel(), Lang.TURTLE), rootPredicate.getURI(), predicates.stream().map(Resource::getURI).collect(joining(","))));
            return createDefaultModel();
        }

        return findDepth(model, newSubject, predicates.stream().filter(p -> !p.equals(rootPredicate)).collect(Collectors.toList()));
    }

}
