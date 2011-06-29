

package com.unknown.entity.items;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Items {

    private final int id;
    private final String itemname;
    private int wowid;
    private double price;
    private String slot;
    private Type type;
    private int wowid_hc;
    private double price_hc;
    private int ilvl;
    private List<ItemLooter> looterList = new ArrayList<ItemLooter>();
    private String quality;

    public Items(int id, String itemname, int wowid, double price, int wowid_hc, double price_hc, String slot, Type type, int ilvl, String quality) {
            this.id = id;
            this.itemname = itemname;
            this.wowid = wowid;
            this.price = price;
            this.wowid_hc = wowid_hc;
            this.price_hc = price_hc;
            this.slot = slot;
            this.type = type;
            this.ilvl = ilvl;
            this.quality = quality;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return itemname;
    }
    public int getWowId() {
        return wowid;
    }
    public int getWowId_hc() {
        return wowid_hc;
    }
    public double getPrice_hc() {
        return price_hc;
    }
    public double getPrice() {
        return price;
    }
    public String getSlot() {
        return slot;
    }
    public Type getType() {
        return type;
    }
    public int getIlvl() {
            return ilvl;
    }
    public void setIlvl(int ilvl) {
            this.ilvl = ilvl;
    }

    public String getQuality() {
            return quality;
    }
    public void setQuality(String quality) {
            this.quality = quality;
    }
    public void addLooterList(Collection<ItemLooter> looters) {
                looterList.addAll(looters);
        }

        public ImmutableList<ItemLooter> getLooterList() {
                return ImmutableList.copyOf(looterList);
        }
}
