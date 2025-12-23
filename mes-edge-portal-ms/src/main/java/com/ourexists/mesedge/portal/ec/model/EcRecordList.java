package com.ourexists.mesedge.portal.ec.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EcRecordList {

    private List<Map<String, String>> data;

    private Map<String, String> calc;
}
