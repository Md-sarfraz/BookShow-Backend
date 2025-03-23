package com.jwtAuthentication.jwt.service;

import com.cloudinary.Cloudinary;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageServiceImpl implements CloudinaryImageService {
    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
            public CloudinaryImageServiceImpl(Cloudinary cloudinary, UserRepository userRepository) {
                  this.cloudinary = cloudinary;
                this.userRepository = userRepository;
            }
    @Override
    public Map upload(MultipartFile file ,int id) {
        try {
           Map data= this.cloudinary.uploader().upload(file.getBytes(),Map.of());
            String imageUrl = (String) data.get("secure_url");

            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            user.setImage(imageUrl);
            userRepository.save(user); // Save updated user
            return Map.of("success", true, "imageUrl", imageUrl, "user", user);

        } catch (IOException e) {
            throw new RuntimeException("image upload failed");
        }
    }
}
