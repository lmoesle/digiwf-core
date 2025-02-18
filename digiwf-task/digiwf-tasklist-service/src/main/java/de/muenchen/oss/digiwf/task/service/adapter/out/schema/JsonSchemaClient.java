package de.muenchen.oss.digiwf.task.service.adapter.out.schema;

import org.openapitools.configuration.ClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@FeignClient(
    name = "${feign.client.config.jsonschema.name:jsonschema}",
    url = "${feign.client.config.jsonschema.url:${feign.client.config.default.url:http://localhost:8080/engine-rest}}",
    configuration = {ClientConfiguration.class}
)
public interface JsonSchemaClient {

  @RequestMapping(value = "/rest/jsonschema/{schemaId}", method = {RequestMethod.GET}, produces = {"application/json"})
  Map<String, Object> getSchemaById(@PathVariable("schemaId") String schemaId);
}
