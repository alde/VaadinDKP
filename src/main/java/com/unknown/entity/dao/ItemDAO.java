/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.unknown.entity.dao;

import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.items.ItemPrices;
import com.unknown.entity.items.Items;
import com.unknown.entity.items.Multiplier;
import com.unknown.entity.raids.RaidItem;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */

public interface ItemDAO {

    public List<Items> getItems();

        public List<ItemPrices> getDefaultPrices() throws SQLException;

        public int updateItem(Items item, String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc, int ilvl);

        public int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type, int ilvl) throws SQLException;

        public Object getItemPrice(String itemname, boolean heroic) throws SQLException;

        public Items getSingleItem(String name);

        public void updateDefaultPrice(String slot, double normalprice);

        public int deleteItem(int id) throws SQLException;

        public ArrayList<RaidItem> getItemsForRaid(int id) throws SQLException;

        public int getItemId(String name);

        public Items getItemById(int id);

        public int getLootId(int itemid, int charid, double price, Boolean heroic, int raidid);

        public void updateLoots(Items item, String price, String pricehc);

        public List<Multiplier> getMultipliers();

        public void addMultiplier(int ilvl, double multiplier);

        public Multiplier getMultiplierForItemlevel(int ilvl);

        public void updateItemLevels(int id, int ilvl, double multiplier);

        public void deleteItemLevelsMultiplier(int id);

        public double getDefaultPrice(Items item);
}
