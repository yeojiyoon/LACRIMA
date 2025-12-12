package com.example.demo.game;

import jakarta.persistence.*;

@Entity
@Table(name = "shop_item")
public class ShopItem {

    @Id
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "item_effect", length = 1000)
    private String itemEffect;

    @Column(name = "item_description", nullable = false, length = 1000)
    private String itemDescription;

    @Column(name = "item_cost", nullable = false)
    private Integer itemCost;

    @Column(name = "item_remain", nullable = false)
    private Integer itemRemain;

    @Column(name = "is_onsale", nullable = false)
    private Boolean isOnsale;

    public ShopItem() {}

    public ShopItem(String itemName, String displayName, String imageUrl,
                    String itemEffect, String itemDescription, Integer itemCost, Integer itemRemain, Boolean isOnsale) {
        this.itemName = itemName;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.itemEffect = itemEffect;
        this.itemDescription = itemDescription;
        this.itemCost = itemCost;
        this.itemRemain = itemRemain;
        this.isOnsale = isOnsale;
    }

    public ShopItem(String itemName, String displayName, String imageUrl, String itemEffect, String itemDescription, int itemCost, int itemRemain, Object o, Boolean isOnsale) {
    }

    public String getItemName() { return itemName; }
    public String getDisplayName() { return displayName; }
    public String getImageUrl() { return imageUrl; }
    public String getItemEffect() { return itemEffect; }
    public String getItemDescription() { return itemDescription; }
    public Integer getItemCost() { return itemCost; }
    public Integer getItemRemain() { return itemRemain; }
    public Boolean getIsOnsale() { return isOnsale; }

    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setItemEffect(String itemEffect) { this.itemEffect = itemEffect; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public void setItemCost(Integer itemCost) { this.itemCost = itemCost; }
    public void setItemRemain(Integer itemRemain) { this.itemRemain = itemRemain; }
    public void setIsOnsale(Boolean isOnsale) { this.isOnsale = isOnsale; }
}
