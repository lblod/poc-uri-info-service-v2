package mu.semte.ch.uriinfo.v2.app.service;

import lombok.extern.slf4j.Slf4j;
import mu.semte.ch.uriinfo.v2.app.error.UINotDefinedForSubjectException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class InMemoryTripleStoreService {

    private final Dataset metaDatatabase;

    public InMemoryTripleStoreService(Dataset metaDatatabase) {
        this.metaDatatabase = metaDatatabase;
    }

    public void write(String nameUri, Model model) {
        try {
            metaDatatabase.removeNamedModel(nameUri);
            metaDatatabase.begin(ReadWrite.WRITE);
            metaDatatabase.addNamedModel(nameUri,model);
            metaDatatabase.commit();
        } catch (Throwable e) {
            log.error("error while writing in memory triplestore", e);
            metaDatatabase.abort();
        } finally {
            metaDatatabase.end();
        }
    }

    public Model getNamedModel(String nameUri) {
        Model model = ModelFactory.createDefaultModel();
        metaDatatabase.begin(ReadWrite.READ);
        Optional.ofNullable(metaDatatabase.getNamedModel(nameUri)).ifPresent(model::add);
        metaDatatabase.end();

        if(model.isEmpty()) {
            throw new UINotDefinedForSubjectException();
        }

        return model;
    }

}
