/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.dao.*;
import com.unknown.entity.database.*;
import com.unknown.entity.character.*;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidLootEditWindow extends Window {

        Raid raid;
        RaidItem item;
        RaidDAO raidDao;
        CharacterDAO characterDao;
        ItemDAO itemDao;
        private List<RaidLootListener> listeners = new ArrayList<RaidLootListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();

        public RaidLootEditWindow(Raid raid, RaidItem item) {
                this.item = item;
                this.raidDao = new RaidDB();
                this.raid = raid;
                this.characterDao = new CharacterDB();
                this.itemDao = new ItemDB();
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
                this.setCaption("Edit loot: " + item.getName() + " for raid : " + raid.getComment());
                this.setPositionX(600);
                this.setPositionY(300);
        }

        public void printInfo() {
                final ComboBox charname = new ComboBox("Name");
                charname.setWidth("300px");
                charname.addStyleName("select-button");
                final ComboBox itemname = new ComboBox("Item");
                itemname.setWidth("300px");
                itemname.addStyleName("select-button");
                final CheckBox heroic = new CheckBox("Heroic");
                final TextField price = new TextField("Price");
                final Button deleteButton = new Button("Remove");
                final Button updateButton = new Button("Update");

                HorizontalLayout hzl = new HorizontalLayout();
                addComponent(charname);
                addComponent(itemname);
                addComponent(heroic);
                addComponent(price);
                hzl.addComponent(deleteButton);
                hzl.addComponent(updateButton);
                addComponent(hzl);
                for (RaidChar rc : raid.getRaidChars()) {
                        charname.addItem(rc.getName());
                }
                charname.setValue(item.getLooter());
                charname.setImmediate(true);
                charname.setNullSelectionAllowed(false);

                for (Items i : itemDao.getItems()) {
                        itemname.addItem(i.getName());
                }
                itemname.setValue(item.getName());
                itemname.setImmediate(true);
                itemname.setNullSelectionAllowed(false);

                heroic.setValue(item.isHeroic());
                price.setValue(item.getPrice());
                price.setImmediate(true);
                heroic.setImmediate(true);

                deleteButton.addListener(new DeleteItemListener());
                updateButton.addListener(new UpdateItemListener(charname, itemname, price, item.getId(), heroic));

        }

        public void addRaidLootInfoListener(RaidLootListener listener) {
                listeners.add(listener);
        }

        public void addCharacterInfoListener(CharacterInfoListener lstnr)  {
                charinfolisteners.add(lstnr);
        }

        private void notifyListeners() {
                for (RaidLootListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
                for (CharacterInfoListener cinfoListener : charinfolisteners) {
                        cinfoListener.onCharacterInfoChange();
                }
        }

        private int deleteItem(RaidItem item) throws SQLException {
                return raidDao.removeLootFromRaid(item);
        }

        private class DeleteItemListener implements ClickListener {
                @Override
                public void buttonClick(ClickEvent event) {
                        try {
                                // System.out.println("Reward Item ID: " + item.getId());
                                int success = deleteItem(item);
                                // System.out.println(success + "items deleted.");
                                notifyListeners();
                        } catch (SQLException ex) {
                                Logger.getLogger(RaidLootEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }

                }
        }

        private class UpdateItemListener implements ClickListener {
                private final TextField price;
                private final CheckBox isheroic;
                private final int id;
                private final ComboBox looter;
                private final ComboBox itemname;
                
                
                UpdateItemListener(ComboBox looter, ComboBox itemname, TextField price, int id, CheckBox isheroic)  {
                        this.price = price;
                        this.looter = looter;
                        this.itemname = itemname;
                        this.id = id;
                        this.isheroic = isheroic;
                }
                @Override
                public void buttonClick(ClickEvent event) {
                        double newprice = Double.parseDouble(price.getValue().toString());
                        boolean heroic = Boolean.parseBoolean(isheroic.getValue().toString());
                        String newlooter = looter.getValue().toString();
                        String newitem = itemname.getValue().toString();
                                // System.out.println("Reward Item ID: " + id);
                                // System.out.println("Reward Item Name: " + newitem + "\nLooter: " + newlooter + "\nPrice: " + newprice + "\nHeroic: " + heroic);
                                int success = updateItem(id, newlooter, newitem, newprice, heroic);
                                // System.out.println(success + " items updated.");
                                notifyListeners();
                }

                private int updateItem(int id, String looter, String itemname, double price, boolean heroic) {
                        return raidDao.doUpdateLoot(id, looter, itemname, price, heroic);
                }
        }
}
