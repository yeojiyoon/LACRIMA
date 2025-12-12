package com.example.demo.admin;

import com.example.demo.game.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/shop")
public class AdminShopController {

    private final ShopItemRepository shopItemRepository;

    public AdminShopController(ShopItemRepository shopItemRepository) {
        this.shopItemRepository = shopItemRepository;
    }

    // ===== 목록 =====
    @GetMapping
    public String list(Model model) {
        model.addAttribute("shopItems", shopItemRepository.findAll());
        return "admin/shop/list"; // templates/admin/shop/members.html
    }

    // ===== 신규 폼 =====
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("shopItemForm", new ShopItemForm());
        model.addAttribute("editMode", false);
        return "admin/shop/form"; // templates/admin/shop/form.html
    }

    // ===== 신규 저장 =====
    @PostMapping("/new")
    public String create(@ModelAttribute("shopItemForm") ShopItemForm form) {
        ShopItem shopItem = new ShopItem(
                form.getItemName(),
                form.getDisplayName(),
                form.getImageUrl(),
                form.getItemEffect(),
                form.getItemDescription(),
                form.getItemCost(),
                form.getItemRemain(),
                form.getIsOnsale()
        );
        shopItemRepository.save(shopItem);
        return "redirect:/admin/shop";
    }

    // ===== 수정 폼 =====
    @GetMapping("/{itemName}/edit")
    public String editForm(@PathVariable String itemName, Model model) {
        ShopItem shopItem = shopItemRepository.findById(itemName)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemName));

        ShopItemForm form = new ShopItemForm();
        form.setItemName(shopItem.getItemName());
        form.setDisplayName(shopItem.getDisplayName());
        form.setImageUrl(shopItem.getImageUrl());
        form.setItemEffect(shopItem.getItemEffect());
        form.setItemDescription(shopItem.getItemDescription());
        form.setItemCost(shopItem.getItemCost());
        form.setItemRemain(shopItem.getItemRemain());
        form.setIsOnsale(shopItem.getIsOnsale());

        model.addAttribute("shopItemForm", form);
        model.addAttribute("editMode", true);
        model.addAttribute("itemName", itemName); // form.html에서 action에 사용
        return "admin/shop/form";
    }

    // ===== 수정 저장 =====
    @PostMapping("/{itemName}/edit")
    public String update(@PathVariable String itemName,
                         @ModelAttribute("shopItemForm") ShopItemForm form) {

        ShopItem shopItem = shopItemRepository.findById(itemName)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemName));

        // PK(itemName)는 수정 불가로 두는 걸 권장 (form에서도 readonly)
        shopItem.setDisplayName(form.getDisplayName());
        shopItem.setImageUrl(form.getImageUrl());
        shopItem.setItemEffect(form.getItemEffect());
        shopItem.setItemDescription(form.getItemDescription());
        shopItem.setItemCost(form.getItemCost());
        shopItem.setItemRemain(form.getItemRemain());
        shopItem.setIsOnsale(form.getIsOnsale());

        shopItemRepository.save(shopItem);
        return "redirect:/admin/shop";
    }

    // ===== 삭제 =====
    @PostMapping("/{itemName}/delete")
    public String delete(@PathVariable String itemName) {
        shopItemRepository.deleteById(itemName);
        return "redirect:/admin/shop";
    }

    @PostMapping("/{itemName}/toggle")
    public String toggleOnsale(@PathVariable String itemName) {

        ShopItem shopItem = shopItemRepository.findById(itemName)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemName));

        // 🔁 true ↔ false 토글
        shopItem.setIsOnsale(!shopItem.getIsOnsale());

        shopItemRepository.save(shopItem);

        return "redirect:/admin/shop";
    }

}
