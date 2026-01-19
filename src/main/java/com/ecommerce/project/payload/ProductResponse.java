package com.ecommerce.project.payload;

import java.util.List;

public class ProductResponse {
    public List<ProductDTO> getContent() {
        return content;
    }

    public void setContent(List<ProductDTO> content) {
        this.content = content;
    }

    private List<ProductDTO> content;

}
