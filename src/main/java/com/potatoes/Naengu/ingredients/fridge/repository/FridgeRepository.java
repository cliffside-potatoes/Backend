package com.potatoes.Naengu.ingredients.fridge.repository;

import com.potatoes.Naengu.ingredients.fridge.domain.model.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

}
