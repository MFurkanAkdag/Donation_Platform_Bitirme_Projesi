package com.seffafbagis.api.dto.request.application;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompleteApplicationRequest {

    private String completionNotes;

    private BigDecimal actualAmountProvided;
}
