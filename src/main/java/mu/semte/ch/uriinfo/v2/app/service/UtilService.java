package mu.semte.ch.uriinfo.v2.app.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.RootSubjectType;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.SplitIRI;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_FIELDS;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SEPARATOR;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.P_SOURCE;

@Service
@Slf4j
public class UtilService {

  private final InMemoryTripleStoreService inMemoryTripleStoreService;
  private final UriInfoService uriInfoService;

  public UtilService(InMemoryTripleStoreService inMemoryTripleStoreService,
                     UriInfoService uriInfoService) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
    this.uriInfoService = uriInfoService;
  }

  public String buildTitle(Model metaModel, String uri, String typeUri, String resourceUri, Property titleProperty) {
    Property resourceProp = ResourceFactory.createProperty(resourceUri);
    var title = metaModel.getRequiredProperty(resourceProp, titleProperty);
    String separator = ofNullable(metaModel.getProperty(resourceProp, P_SEPARATOR)).map(Statement::getString).orElse(" ");
    RDFList titleParts = metaModel.getList(title.getObject().asResource());
    return titleParts.asJavaList().stream().map(titlePart -> {
      if (titlePart.isLiteral()) {
        return titlePart.asLiteral().getString();
      }
      Resource titleField = titlePart.asResource();
      return queryForField(titleField, metaModel, uri, typeUri);
    }).collect(Collectors.joining(separator));
  }


  public Optional<List<RootSubjectType>> fetchSource(Model metaModel,
                                                     Resource resource,
                                                     String rootSubject,
                                                     String rootType) {
    RootSubjectType rst = new RootSubjectType();
    rst.subject = rootSubject;
    rst.type = rootType;
    Optional<Resource> sourceProperty = ofNullable(metaModel.getProperty(resource, P_SOURCE)).map(Statement::getResource);
    if (sourceProperty.isPresent()) {
      var sources = metaModel.getList(sourceProperty.get()).iterator();
      while (sources.hasNext()) {
        var source = sources.next().asResource();
        List<Map<String, String>> result = this.uriInfoService.fetchSource(rst.subject, source.getURI(), rst.type);
        if (result.isEmpty()) {
          return Optional.empty();
        }
        if (result.size() > 1 && !sources.hasNext()) {
          List<RootSubjectType> rsts = result.stream()
                                             .map(res -> RootSubjectType.builder()
                                                                        .subject(res.get("subject"))
                                                                        .type(res.get("type"))
                                                                        .build())
                                             .collect(Collectors.toList());
          return Optional.of(rsts);
        }
        else if (result.size() == 1) {
          Map<String, String> res = result.get(0);
          if (res.isEmpty() || StringUtils.isAnyEmpty(res.get("type"), res.get("subject"))) {
            return Optional.empty();
          }
          rst.subject = res.get("subject");
          rst.type = res.get("type");
        }
/*        else {
          throw new RuntimeException("todo refactor"); //todo
        }*/
      }
    }
    return Optional.of(List.of(rst));
  }


  public String queryForField(Resource fieldResource, Model metaModel, String uri, String typeUri) {
    var rootSubject = uri;
    var rootType = typeUri;

    Statement fieldsProperty = metaModel.getRequiredProperty(fieldResource, P_FIELDS);
    String separator = ofNullable(metaModel.getProperty(fieldResource, P_SEPARATOR)).map(Statement::getString).orElse(" ");
    //multi level field
    Optional<RootSubjectType> rootSubjectType = fetchSource(metaModel, fieldResource, rootSubject, rootType).flatMap(rsts -> rsts
            .stream()
            .findFirst());
    if (rootSubjectType.isPresent()) {
      RootSubjectType rst = rootSubjectType.get();
      rootSubject = rst.getSubject();
      rootType = rst.getType();
    }
    else {
      return null;
    }
    //end multi level field

    RDFList innerFields = metaModel.getList(fieldsProperty.getResource());
    Map<String, String> variableQuery = innerFields.asJavaList().stream().map(RDFNode::asResource)
                                                   .map(Resource::getURI)
                                                   .map(iri -> Map.entry(SplitIRI.localname(iri), iri))
                                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (s, s2) -> s, LinkedHashMap::new));

    var result = this.uriInfoService.dynamicQuery(rootSubject, rootType, variableQuery);

    return result.stream().findFirst().map(m -> String.join(separator, m.values())).orElse(null);
  }



  //todo add to mu-java
  public static void extractFromModel(Resource subject, Model model, Model newModel){
    Model m = model.listStatements(subject, null, (RDFNode) null).toModel();
    newModel.add(m);
    m.listStatements().toList().stream()
     .filter(statement -> statement.getObject().isResource())
     .map(statement -> statement.getObject().asResource())
     .forEach(s-> extractFromModel(s, model, newModel));
  }

}
