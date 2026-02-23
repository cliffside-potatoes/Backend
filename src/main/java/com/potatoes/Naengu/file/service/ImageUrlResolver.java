package com.potatoes.Naengu.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlResolver {

    @Value("${cloud.s3.cdn-base-url}")
    private String baseUrl;

    public String resolve(String s3Key){
        if(s3Key==null || s3Key.isBlank()) return null;
        return baseUrl.endsWith("/") ? (baseUrl + s3Key) : (baseUrl + "/" + s3Key);
    }

}
