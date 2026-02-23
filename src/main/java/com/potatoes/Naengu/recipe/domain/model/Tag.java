package com.potatoes.Naengu.recipe.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tag",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tag_value", columnNames = "value")
        }
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String value;

    private Tag(String value) {
        validate(value);
        this.value = value;
    }

    public static Tag of(String value) {
        return new Tag(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tag.value must not be blank");
        }
        if (value.trim().length() > 50) {
            throw new IllegalArgumentException("tag.value must be <= 50 chars");
        }
    }
}
