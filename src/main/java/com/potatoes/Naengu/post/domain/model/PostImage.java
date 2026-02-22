package com.potatoes.Naengu.post.domain.model;

import com.potatoes.Naengu.profile.dto.ProfileImageRequest;
import jakarta.persistence.*;

@Entity
@Table(name = "post_image")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String accessType;

    protected PostImage() {}

    public PostImage(String s3Key, String contentType, Long size, String accessType) {
        this.s3Key = s3Key;
        this.contentType = contentType;
        this.size = size;
        this.accessType = accessType;
    }

    public void update(ProfileImageRequest req){
        this.s3Key = req.s3Key();
        this.contentType = req.contentType();
        this.size = req.size();
        this.accessType = req.accessType();
    }
}
