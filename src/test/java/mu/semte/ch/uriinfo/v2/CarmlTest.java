package mu.semte.ch.uriinfo.v2;


import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.StringWriter;

@Slf4j
public class CarmlTest {

    @SneakyThrows
    @Test
    public void testJsonMapping() {
        var source = new ClassPathResource("input.json").getInputStream();
        var mappingDefinition = new ClassPathResource("mapping.ttl").getInputStream();
        var loader = RmlMappingLoader
                .build();

        var mapper = RmlMapper.newBuilder()
                .setLogicalSourceResolver(Rdf.Ql.JsonPath, new JsonPathResolver())
                .classPathResolver("/tmp")
                .build();
        mapper.bindInputStream("input", source);

        var mapping = loader.load(RDFFormat.TURTLE, mappingDefinition);
        Model result = mapper.map(mapping);
        StringWriter writer = new StringWriter();
        Rio.write(result, writer, RDFFormat.TURTLE);
        log.info(writer.toString());

    }
}
