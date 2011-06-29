
package com.unknown.entity.items;

import com.unknown.entity.database.ItemDB;
import java.util.List;


public class DefaultPrices {
	
    public List<ItemPrices> getPrices() {
           return ItemDB.getDefaultPrices();
    }
}
