package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.service.CloudinaryImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/cloudinary/upload")
public class CloudinaryImageController {

    private final CloudinaryImageService cloudinaryImageService;

    public CloudinaryImageController(CloudinaryImageService cloudinaryImageService) {
        this.cloudinaryImageService = cloudinaryImageService;
    }

    @PostMapping
    public ResponseEntity<Map> uploadImg(@RequestParam("image") MultipartFile file){
        Map data=cloudinaryImageService.upload(file);
        return new ResponseEntity<>(data,HttpStatus.OK);
    }
}
