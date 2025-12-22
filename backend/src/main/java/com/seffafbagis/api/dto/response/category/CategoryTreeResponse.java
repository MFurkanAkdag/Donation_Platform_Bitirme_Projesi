package com.seffafbagis.api.dto.response.category;

import java.util.ArrayList;
import java.util.List;

public class CategoryTreeResponse extends CategoryResponse {
    private List<CategoryTreeResponse> children = new ArrayList<>();

    public List<CategoryTreeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryTreeResponse> children) {
        this.children = children;
    }
}
