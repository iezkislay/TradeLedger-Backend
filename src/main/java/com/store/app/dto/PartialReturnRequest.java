package com.store.app.dto;

import com.store.app.enums.ReturnSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class PartialReturnRequest {

    @NotNull
    private UUID billItemId;

    @NotNull
    private ReturnSource returnSource;

    @NotNull
    @Positive
    private BigDecimal quantity;

    private String reason;

}
