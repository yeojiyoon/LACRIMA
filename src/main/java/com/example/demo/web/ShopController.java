package com.example.demo.web;

import com.example.demo.game.ShopItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopController {

    private final ShopItemRepository shopItemRepository;

    public ShopController(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("shopItems", shopItemRepository.findByIsOnsaleTrueOrderByDisplayNameAsc());
        return "index"; // templates/shop/index.html
    }
}
