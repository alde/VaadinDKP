/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.unknown.entity.dao;

import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.items.ItemPrices;
import com.unknown.entity.items.Items;
import com.unknown.entity.raids.RaidItem;
import java.sql.Connection;
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

        public int updateItem(Items item, String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc, boolean legendary);

        public int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type, boolean isLegendary) throws SQLException;

        public Object getItemPrice(String itemname, boolean heroic) throws SQLException;

        public int getItemId(Connection c, String loot) throws SQLException;

        public Items getSingleItem(String name);

        public void updateDefaultPrice(String slot, double normalprice, double heroicprice);

        public int deleteItem(int id) throws SQLException;

        public ArrayList<RaidItem> getItemsForRaid(int id) throws SQLException;

        public int getItemId(String name);

}
