package com.ecommerce.project.payload;


import java.util.List;


public class CategoryResponse {
    private List<CategoryDTO> content;
    public void setContent(List<CategoryDTO>content){
        this.content = content;
    }

}
