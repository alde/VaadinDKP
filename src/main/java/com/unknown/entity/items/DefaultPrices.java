/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.dao.ItemDAO;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author alde
 */
public class DefaultPrices {

	private final ItemDAO itemDao;

	public DefaultPrices(ItemDAO itemDao) {
		this.itemDao = itemDao;
	}
	
    public List<ItemPrices> getPrices() throws SQLException {
           return itemDao.getDefaultPrices();
    }
}
