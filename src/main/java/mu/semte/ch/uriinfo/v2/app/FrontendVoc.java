package mu.semte.ch.uriinfo.v2.app;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public interface FrontendVoc {
    Property P_PAGES = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/pages");
    Property P_MAIN_PAGE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/mainPage");
    Property P_HAS_LINK = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/hasLink");
    Property P_SOURCE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/source");
    Property P_DETAIL_PANEL = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/detailPanel");
    Property P_META_FIELD = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/metaField");
    Property P_CONTENT_TYPE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/contentType");
    Property P_FIELDS = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/fields");
    Property P_SUBJECTS = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/subjects");
    Property P_LABEL = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/label");
    Property P_SEPARATOR = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/separator");
    Property P_ORDERING = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/ordering");
    Property P_FIELD_TYPE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/fieldType");
    Property P_TITLE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/title");
    Property P_EDITABLE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/editable");
    Property P_TYPE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/type");
    Property P_ELEMENTS = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/elements");
    Property P_SIDE_PANEL_TITLE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/sidePanelTitle");

    Resource C_FIELD = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/Field");
    Resource C_LINK = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/Link");
    Resource C_ELEMENT = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/Element");
    Resource C_MULTI_LEVEL_FIELD = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/MultiLevelField");
    Resource C_SIDE_PANEL_LINK_FIELD = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/SidePanelLinkField");
    Resource C_SIDE_PANEL_TITLE = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/SidePanelTitle");
    Resource C_SIDE_PANEL = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/SidePanel");
    Resource C_PAGE = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/Page");
    Resource C_UI = ResourceFactory.createResource("http://lblod.data.gift/vocabularies/frontend/UI");

    Resource C_TABLE = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/Table");
    Resource C_PANEL = ResourceFactory.createProperty("http://lblod.data.gift/vocabularies/frontend/Panel");
}
