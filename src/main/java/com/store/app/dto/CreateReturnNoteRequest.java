package com.store.app.dto;

import java.util.List;

public class CreateReturnNoteRequest {

    private List<ReturnItemRequest> items;

    public List<ReturnItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemRequest> items) {
        this.items = items;
    }
}
