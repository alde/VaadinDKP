

package com.unknown.entity.items;

import com.unknown.entity.Slots;


public class ItemPrices {

    private Slots slot;
    private double price;
    private double price_heroic;

    public ItemPrices(Slots slot, double price, double price_heroic) {
        this.slot = slot;
        this.price = price;
        this.price_heroic = price_heroic;
    }

    public double getPrice() {
        return price;
    }

    public double getPriceHeroic() {
        return price_heroic;
    }

    public String getSlotString() {
        return slot.toString();
    }

}
