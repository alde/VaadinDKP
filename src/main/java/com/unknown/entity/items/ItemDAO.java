/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.unknown.entity.items;

import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author alde
 */

public interface ItemDAO {

    public List<Items> getItems();

        public List<ItemPrices> getDefaultPrices() throws SQLException;

        public int updateItem(Items item, String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc, boolean legendary);
}
