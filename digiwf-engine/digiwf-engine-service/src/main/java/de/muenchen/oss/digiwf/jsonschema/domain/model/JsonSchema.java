/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */

package de.muenchen.oss.digiwf.jsonschema.domain.model;

import de.muenchen.oss.digiwf.json.factory.JsonSchemaFactory;
import lombok.*;

import jakarta.persistence.*;
import java.util.Map;

/**
 * Entity representation of a form.
 *
 * @author externer.dl.horn
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "JsonSchema")
@Table(name = "DWF_JSON_SCHEMA", indexes = {@Index(name = "IDX_DWF_SCHEMAKEY", columnList = "key_")})
public class JsonSchema {

    @Id
    @Column(name = "key_")
    private String key;

    @Column(name = "schema_", columnDefinition = "CLOB")
    private String schema;

    public Map<String, Object> getSchemaMap() {
        return JsonSchemaFactory.gson().fromJson(this.schema, JsonSchemaFactory.mapType());
    }
}
