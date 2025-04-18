package com.example.discount;

public class PercentageDiscount implements Discount {
    private final double percentage;

    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double apply(double price) {
        return price * (1 - percentage);
    }
}
