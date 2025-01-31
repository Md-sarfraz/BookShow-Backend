package com.jwtAuthentication.jwt.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.Document;

@Service
public class DocumentService {
    public Document saveDocument(Document document, MultipartFile file) {
        // Implement document saving logic here
        return document; // Return the updated document object
    }
}
