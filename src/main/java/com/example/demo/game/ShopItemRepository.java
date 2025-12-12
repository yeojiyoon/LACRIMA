package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopItemRepository extends JpaRepository<ShopItem, String> {
    List<ShopItem> findByIsOnsaleTrueOrderByDisplayNameAsc();
}
