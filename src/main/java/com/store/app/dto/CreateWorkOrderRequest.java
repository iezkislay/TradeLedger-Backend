package com.store.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateWorkOrderRequest {

    private UUID customerId;
    private String description;

}
