package mu.semte.ch.uriinfo.v2.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.lib.utils.ModelUtils;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendForm;
import mu.semte.ch.uriinfo.v2.app.dto.form.Input;
import mu.semte.ch.uriinfo.v2.app.dto.form.InputText;
import mu.semte.ch.uriinfo.v2.app.dto.form.InputType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.apache.jena.assembler.Mode;
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
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static mu.semte.ch.uriinfo.v2.app.FrontendVoc.*;

@Service
@Slf4j
public class UIFormService {
  private final InMemoryTripleStoreService inMemoryTripleStoreService;
  private final UriInfoService uriInfoService;
  private final UtilService utilService;

  public UIFormService(InMemoryTripleStoreService inMemoryTripleStoreService,
                       UriInfoService uriInfoService, UtilService utilService) {
    this.inMemoryTripleStoreService = inMemoryTripleStoreService;
    this.uriInfoService = uriInfoService;
    this.utilService = utilService;
  }

  public FrontendForm buildForm(String uri, String formUri) {
    FrontendForm form = new FrontendForm();
    String typeUri = uriInfoService.fetchType(uri);
    var metaModel = inMemoryTripleStoreService.getNamedModel(typeUri);
    form.setTitle(this.utilService.buildTitle(metaModel, uri, typeUri, formUri, P_TITLE));
    Property formPart = ResourceFactory.createProperty(formUri);
    form.setOrdering(metaModel.getProperty(formPart, P_ORDERING).getInt());
    form.setFormUri(formUri);
    form.setUri(uri);
    form.setTypeUri(typeUri);

    var mapper = new ObjectMapper();
    ObjectNode skeleton = mapper.createObjectNode();
    ObjectNode typeObj = skeleton.putObject(CaseUtils.toCamelCase(SplitIRI.localname(typeUri),false));
    typeObj.put("id",SplitIRI.localname(uri));

    var inputsProp = metaModel.getRequiredProperty(formPart, P_FORM_FIELDS);
    var inputParts = metaModel.getList(inputsProp.getObject().asResource()).asJavaList();
    form.setInputs(inputParts.stream()
                             .map(RDFNode::asResource)
                             .map(inputPart -> {
                               Statement elementTypeStmt = metaModel.getRequiredProperty(inputPart, RDF.type);
                               InputType inputType = InputType.evaluateType(elementTypeStmt.getResource());
                               Input input = switch (inputType) {
                                 case TEXT, DATE, NUMBER, TIME -> this.buildInput(metaModel, uri, typeUri, inputPart, inputType, skeleton);
                                 case SELECT, MULTI_SELECT -> null;
                               };
                               return input;
                             }).collect(Collectors.toList()));

    form.setSkeleton(skeleton);

    return form;
  }



  private Input buildInput(Model metaModel,
                           String uri,
                           String typeUri,
                           Resource inputPart,
                           InputType inputType,
                           ObjectNode skeleton) {
    String rootProp = CaseUtils.toCamelCase(SplitIRI.localname(typeUri), false);
    ObjectNode rootNode = (ObjectNode)skeleton.get(rootProp);
    var builder = InputText.builder();
    var ordering = metaModel.getProperty(inputPart, P_ORDERING).getInt();
    var label = metaModel.getProperty(inputPart, P_LABEL).getString();
    var predicate = metaModel.getProperty(inputPart, P_PREDICATE).getResource();
    var fieldValue = this.utilService.queryForField(inputPart,metaModel, uri, typeUri);

    Optional<Resource> sourceProperty = ofNullable(metaModel.getProperty(inputPart, P_SOURCE)).map(Statement::getResource);

    if(sourceProperty.isPresent()){
      var sources = metaModel.getList(sourceProperty.get());
      List<RDFNode> list = sources.asJavaList();
      var subject = uri;
      var type = typeUri;
      for(RDFNode node: list){
        var source = node.asResource();

        var lastFieldProp = SplitIRI.localname(source.getURI());

        if(!skeleton.has(lastFieldProp)){
          skeleton.putObject(lastFieldProp);
        }
        var objectNode = (ObjectNode)skeleton.get(lastFieldProp);

          if (StringUtils.isNotEmpty(subject)){
            List<Map<String, String>> response = uriInfoService.fetchSource(subject, source.getURI(), type);
            Optional<Map<String, String>> res = response.stream().findFirst();
            if (res.isPresent()){
              var r = res.get();
              subject = r.get("subject");
              type = r.get("type");
            }else {
              subject =null;
            }
          }
          objectNode.put("id", StringUtils.isNotEmpty(subject) ? SplitIRI.localname(subject): ModelUtils.uuid());

      }
      RDFNode rdfNode = Iterables.getLast(sources.asJavaList());
      var lastFieldProp = SplitIRI.localname(rdfNode.asResource().getURI());

      var objectNode = (ObjectNode)skeleton.get(lastFieldProp);
      objectNode.put(predicate.getLocalName(), fieldValue);
      builder.skeletonRef(lastFieldProp);


    }else{
      rootNode.put(predicate.getLocalName(), fieldValue);
      builder.skeletonRef(rootProp);
    }

    return switch (inputType){
      case TEXT -> builder.label(label).ordering(ordering)
                            .predicateLocalName(predicate.getLocalName()).value(fieldValue).build();
      default -> null;
    };
  }
}
