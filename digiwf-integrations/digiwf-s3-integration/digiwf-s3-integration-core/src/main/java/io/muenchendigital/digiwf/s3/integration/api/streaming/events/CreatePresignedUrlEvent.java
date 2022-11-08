package io.muenchendigital.digiwf.s3.integration.api.streaming.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePresignedUrlEvent {

    @Pattern(regexp = "GET|PUT|DELETE|POST")
    private String action;
    @NotNull(message = "path is mandatory")
    @NotBlank(message = "path is mandatory")
    private String path;

}
