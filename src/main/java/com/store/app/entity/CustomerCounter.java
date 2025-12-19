package com.store.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_counter")
public class CustomerCounter {

    @Id
    private Integer id; // always 1

    private Integer lastSerial;

    // getters/setters
}
