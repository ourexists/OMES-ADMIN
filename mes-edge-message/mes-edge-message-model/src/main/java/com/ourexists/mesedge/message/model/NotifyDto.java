package com.ourexists.mesedge.message.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class NotifyDto {

    protected String id;

    @NotBlank
    protected String title;

    protected String context;

    @NotNull
    protected Integer type;

    @NotEmpty
    protected List<String> platforms;

    protected Integer step;
}
