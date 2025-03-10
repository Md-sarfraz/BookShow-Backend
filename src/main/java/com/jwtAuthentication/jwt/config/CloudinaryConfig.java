package com.jwtAuthentication.jwt.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlemwjpue",
                "api_key", "523483865836267",
                "api_secret", "VMbeOCWCu7MRPbU_ShJzEhxYg1k"
        ));
    }
}
