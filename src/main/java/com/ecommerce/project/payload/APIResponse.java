package com.ecommerce.project.payload;

public class APIResponse {
    public final String message;

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public final boolean status;

    public APIResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
    }


}
