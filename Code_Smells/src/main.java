package com.example.discount;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Item item1 = new Item("Book", 20, 1, new AmountDiscount(5));
        Item item2 = new TaxableItem("Laptop", 1000, 1, new PercentageDiscount(0.1), 7);
        Item item3 = new GiftCardItem("Gift Card", 10, 1, new AmountDiscount(0));

        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        Order order = new Order(items, "John Doe", "johndoe@example.com");

        System.out.println("Total Price: $" + order.calculateTotalPrice());
        order.sendConfirmationEmail();
        order.printOrder();
    }
}
