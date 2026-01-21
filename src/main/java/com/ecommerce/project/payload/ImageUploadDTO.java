package com.ecommerce.project.payload;

import org.springframework.web.multipart.MultipartFile;

public class ImageUploadDTO {
    private  MultipartFile file;   // optional
    private  String fileName;      // optional
    private  String contentType;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;// optional

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


}
