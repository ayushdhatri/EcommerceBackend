package com.ecommerce.project.service.ImageStorage;

import com.ecommerce.project.payload.ImageUploadDTO;
import com.ecommerce.project.payload.StoredImage;

import java.io.IOException;

public interface ImageStorageStrategy {
    StoredImage uploadImage(ImageUploadDTO request) throws IOException;

}
