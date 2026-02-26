package com.potatoes.Naengu.post.domain.model;

import com.potatoes.Naengu.profile.domain.model.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    //post_image 쪽에 저장될 post의 fk post_image.post_id
    @JoinColumn(name= "post_id", nullable = false)
    @BatchSize(size = 50)
    private List<PostImage> images = new ArrayList<>();

    protected Post() {}

    public Post(Profile profile, String content) {
        this.profile = profile;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addImage(PostImage image) {
        images.add(image);
    }

    public Long getId() {
        return id;
    }
}
