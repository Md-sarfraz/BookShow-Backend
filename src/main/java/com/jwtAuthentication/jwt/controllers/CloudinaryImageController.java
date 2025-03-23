package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.service.CloudinaryImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/cloudinary/upload")
public class CloudinaryImageController {

    private final CloudinaryImageService cloudinaryImageService;

    public CloudinaryImageController(CloudinaryImageService cloudinaryImageService) {
        this.cloudinaryImageService = cloudinaryImageService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadImg(
            @RequestParam("image") MultipartFile file,
            @RequestParam("id") int id) {
        Map<String, Object> data = cloudinaryImageService.upload(file, id);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
