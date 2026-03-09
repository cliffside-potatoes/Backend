package com.potatoes.Naengu.reviewrecipe.domain.model;

import com.potatoes.Naengu.reviewrecipe.dto.RecipeReviewImageRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe_review_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RecipeReviewImage {

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

    @ManyToOne
    @JoinColumn(name = "recipe_review_id", nullable = true)
    private RecipeReview recipeReview;

    public RecipeReviewImage(String s3Key, String contentType, Long size, String accessType, RecipeReview recipeReview) {
        this.s3Key = s3Key;
        this.contentType = contentType;
        this.size = size;
        this.accessType = accessType;
        this.recipeReview = recipeReview;
    }

    public void update(RecipeReviewImageRequest req){
        this.s3Key = req.s3Key();
        this.contentType = req.contentType();
        this.size = req.size();
        this.accessType = req.accessType();
    }

}
