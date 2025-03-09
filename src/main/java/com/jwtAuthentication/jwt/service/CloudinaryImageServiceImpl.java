//package com.jwtAuthentication.jwt.service;
//
//import com.cloudinary.Cloudinary;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//public class CloudinaryImageServiceImpl implements CloudinaryImageService {
//    private final Cloudinary cloudinary;
//            public CloudinaryImageServiceImpl(Cloudinary cloudinary) {
//                  this.cloudinary = cloudinary;
//            }
//    @Override
//    public Map upload(MultipartFile file) {
//        try {
//           Map data= this.cloudinary.uploader().upload(file.getBytes(),Map.of());
//           return data;
//
//        } catch (IOException e) {
//            throw new RuntimeException("image upload failed");
//        }
//    }
//}
