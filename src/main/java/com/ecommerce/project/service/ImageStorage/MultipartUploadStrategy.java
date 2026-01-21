package com.ecommerce.project.service.ImageStorage;

import com.ecommerce.project.payload.ImageUploadDTO;
import com.ecommerce.project.payload.StoredImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class MultipartUploadStrategy implements ImageStorageStrategy{
    @Override
    public StoredImage uploadImage(ImageUploadDTO request) throws IOException {
        // Fine names of the current /orignal file
        String originalFileName = request.getFile().getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));

        // Check if path exist and create
        String filePath = request.getPath() + File.separator + fileName;
        File folder = new File(filePath);
        if(!folder.exists()){
            folder.mkdir();
        }

        Files.copy(request.getFile().getInputStream(), Paths.get(filePath));

        // upload to server
       StoredImage storedImage = new StoredImage();
       storedImage.setFileName(fileName);
       storedImage.setFilePath(filePath);

        // return the stored image
        return storedImage;

    }
}
