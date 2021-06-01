package mu.semte.ch.uriinfo.v2.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.dto.form.FrontendForm;
import mu.semte.ch.uriinfo.v2.app.dto.form.Input;
import mu.semte.ch.uriinfo.v2.app.dto.form.InputText;
import mu.semte.ch.uriinfo.v2.app.dto.form.InputType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
    form.setUri(uri);

    var mapper = new ObjectMapper();
    ObjectNode skeleton = mapper.createObjectNode();
    ObjectNode typeObj = skeleton.putObject(SplitIRI.localname(typeUri).toLowerCase());
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
    var ordering = metaModel.getProperty(inputPart, P_ORDERING).getInt();
    var label = metaModel.getProperty(inputPart, P_LABEL).getString();
    var predicate = metaModel.getProperty(inputPart, P_PREDICATE).getResource();
    var fieldValue = this.utilService.queryForField(inputPart,metaModel, uri, typeUri);
    ObjectNode jsonNode = (ObjectNode)skeleton.get(SplitIRI.localname(typeUri).toLowerCase());
    jsonNode.put(predicate.getLocalName(), fieldValue);
    return switch (inputType){
      case TEXT -> InputText.builder().label(label).ordering(ordering)
                            .metaUri(inputPart.getURI())
                            .predicateUri(predicate.getURI()).value(fieldValue).build();
      default -> null;
    };
  }
}
