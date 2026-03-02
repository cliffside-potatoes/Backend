package com.potatoes.Naengu.fridge.repository;

import com.potatoes.Naengu.fridge.domain.model.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

}
