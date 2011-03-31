/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.database.ItemDB;
import java.util.List;

/**
 *
 * @author alde
 */
public class DefaultPrices {
	
    public List<ItemPrices> getPrices() {
           return ItemDB.getDefaultPrices();
    }
}
