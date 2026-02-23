package com.potatoes.Naengu.ingredients.dictionary.ingredient.domain;

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
@Table(name = "ingredient",
        uniqueConstraints = @UniqueConstraint(name = "uk_ingredient_name", columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private Ingredient(String name) {
        validate(name);
        this.name = normalize(name);
    }

    public static Ingredient of(String name) {
        return new Ingredient(name);
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("ingredient.name must not be blank");
        }

        if (name.trim().length() > 50) {
            throw new IllegalArgumentException("ingredient.name must be <= 50 chars");
        }
    }

    private static String normalize(String name) {
        return name.trim();
    }
}
