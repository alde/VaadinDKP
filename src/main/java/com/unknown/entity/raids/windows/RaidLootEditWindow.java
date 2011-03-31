/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.database.*;
import com.unknown.entity.character.*;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.*;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
public class RaidLootEditWindow extends Window {

        Raid raid;
        RaidItem item;
        private List<RaidLootListener> listeners = new ArrayList<RaidLootListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();
        private List<ItemInfoListener> iteminfolisteners = new ArrayList<ItemInfoListener>();
        private ComboBox charname;
        private ComboBox itemname;
        private TextField price;
        private CheckBox heroic;
        private Application app;

        public RaidLootEditWindow(Raid raid, RaidItem item) {
                this.item = item;
                this.raid = raid;
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
                this.setCaption("Edit loot: " + item.getName() + " for raid : " + raid.getComment());
                this.setPositionX(600);
                this.setPositionY(300);
                this.heroic = new CheckBox("Heroic");
                this.itemname = new ComboBox("Item");
                this.charname = new ComboBox("Name");
                this.price = new TextField("Price");
        }

        private void getDefaultPrice() {
                price.setValue(getItemPrice(itemname.getValue().toString(), heroic.booleanValue()));
        }

        public void printInfo() {
                charname.setWidth("300px");
                itemname.setWidth("300px");
                heroic.addListener(new HeroicCheckBoxListener());
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
                for (String user : CharDB.getUserNames()) {
                        charname.addItem(user);
                }
                charname.setValue(item.getLooter());
                charname.setImmediate(true);
                charname.setNullSelectionAllowed(false);

                for (Items i : ItemDB.getItems()) {
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
                updateButton.addListener(new UpdateItemListener());

        }

        public void addRaidLootInfoListener(RaidLootListener listener) {
                listeners.add(listener);
        }

        public void addCharacterInfoListener(CharacterInfoListener lstnr) {
                charinfolisteners.add(lstnr);
        }

        public void addItemInfoListener(ItemInfoListener listeners) {
                iteminfolisteners.add(listeners);
        }

        private void notifyListeners() {
                for (RaidLootListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
                for (CharacterInfoListener cinfoListener : charinfolisteners) {
                        cinfoListener.onCharacterInfoChange();
                }
                for (ItemInfoListener iteminfolistener : iteminfolisteners) {
                        iteminfolistener.onItemInfoChange();
                }
        }

        private int deleteItem(RaidItem item) {
                // RaidDB.setApplication(app);
                return RaidDB.removeLootFromRaid(item);
        }

        public void addApplication(Application app) {
                this.app = app;
        }

        private class DeleteItemListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        deleteItem(item);
                        notifyListeners();
                        close();
                }
        }

        private Double getItemPrice(String itemname, Boolean isheroic) {
                Double getprice = 0.0;
                if (itemname != null) {
                        getprice = getDefaultPrice(itemname, isheroic.booleanValue());
                }
                return getprice;
        }

        private Double getDefaultPrice(String itemname, boolean isheroic) {
                return (Double) ItemDB.getItemPrice(itemname, isheroic);
        }

        private void doUpdateLoot() {
                double newprice = Double.parseDouble(price.getValue().toString());
                boolean isheroic = Boolean.parseBoolean(heroic.getValue().toString());
                String newlooter = charname.getValue().toString();
                String newitem = itemname.getValue().toString();
                updateItem(newlooter, newitem, newprice, isheroic);
                notifyListeners();
                close();
        }

        private void updateItem(String looter, String itemname, double price, boolean heroic) {
                int lootid = ItemDB.getLootId(item.getId(), CharDB.getCharacterId(item.getLooter()), item.getPrice(), item.isHeroic(), raid.getId());
                // RaidDB.setApplication(app);
                RaidDB.doUpdateLoot(lootid, looter, itemname, price, heroic, raid.getId());
        }

        private class UpdateItemListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        doUpdateLoot();
                }
        }

        private class HeroicCheckBoxListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        getDefaultPrice();
                }
        }
}
