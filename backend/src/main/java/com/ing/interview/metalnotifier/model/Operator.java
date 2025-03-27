package com.ing.interview.metalnotifier.model;

public enum Operator {
    ITEM_IS("Item is"),
    ITEM_IS_NOT("Item is not"),
    PRICE_IS_EQUAL_TO("Price is equal to"),
    PRICE_IS_GREATER_THAN("Price is greater than"),
    PRICE_IS_GREATER_THAN_OR_EQUAL_TO("Price is greater than or equal to"),
    PRICE_IS_LESS_THAN("Price is less than"),
    PRICE_IS_LESS_THAN_OR_EQUAL_TO("Price is less than or equal to");

    private final String displayName;

    Operator(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
