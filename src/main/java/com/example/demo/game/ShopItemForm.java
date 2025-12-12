package com.example.demo.game;

public class ShopItemForm {
    private String itemName;
    private String displayName;
    private String imageUrl;
    private String itemEffect;
    private String itemDescription;
    private int itemCost;
    private int itemRemain;
    private Boolean isOnsale;

    public String getItemName() {return itemName;}
    public void setItemName(String itemName) {this.itemName = itemName;}
    public String getDisplayName() {return displayName;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}
    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}
    public String getItemEffect() {return itemEffect;}
    public void setItemEffect(String itemEffect) {this.itemEffect = itemEffect;}
    public String getItemDescription() {return itemDescription;}
    public void setItemDescription(String itemDescription) {this.itemDescription = itemDescription;}
    public int getItemCost() {return itemCost;}
    public void setItemCost(int itemCost) {this.itemCost = itemCost;}
    public int getItemRemain() {return itemRemain;}
    public void setItemRemain(int itemRemain) {this.itemRemain = itemRemain;}
    public Boolean getIsOnsale() {return isOnsale;}
    public void setIsOnsale(Boolean isOnsale) {this.isOnsale = isOnsale;}
}
