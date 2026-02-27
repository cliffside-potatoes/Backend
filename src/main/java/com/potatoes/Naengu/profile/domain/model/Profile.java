package com.potatoes.Naengu.profile.domain.model;

import com.potatoes.Naengu.ingredients.fridge.domain.model.Fridge;
import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import com.potatoes.Naengu.profile.dto.ProfileImageRequest;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity userEntity;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(length = 150)
    private String bio;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="profile_image_id")
    private ProfileImage profileImage;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fridge_id",nullable = false,unique = true)
    private Fridge fridge;

    protected Profile() {}

    public Profile(UserEntity user, String nickname, String bio, Fridge fridge) {
        this.userEntity = user;
        this.nickname = nickname;
        this.bio = bio;
        this.fridge = fridge;
    }

    public void updateNickname(String nickname) {
        if (nickname != null) this.nickname = nickname;
         }

    public void updateBioIfPresent(String bio) {
        if (bio != null) this.bio = bio;
    }

    //profileImage field update
    public void upsertProfileImageIfPresent(ProfileImageRequest img){
        if (img == null) return;
        if (this.profileImage == null){
            this.profileImage = new ProfileImage(img.s3Key(), img.contentType(), img.size(), img.accessType());

        } else {
            this.profileImage.update(img);
        }

    }

}
