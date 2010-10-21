/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.Items;

/**
 *
 * @author bobo
 */
public class RaidItem {

	private boolean heroic;
	private int id;
	private String looter;
	private String name;
	private double price;

        public RaidItem (String itemname, String looter, int itemid, double price, boolean isheroic) {
                this.name = itemname;
                this.looter = looter;
                this.id = itemid;
                this.price = price;
                this.heroic = isheroic;
        }
        
	public void setId(Integer integer) {
	}

	public boolean isHeroic() {
		return heroic;
	}

	public void setHeroic(boolean heroic) {
		this.heroic = heroic;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLooter() {
		return looter;
	}

	public void setLooter(String looter) {
		this.looter = looter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

        public Items toItem() {
                ItemDAO itemDao = new ItemDB();
                return itemDao.getSingleItem(this.name);
        }
}
