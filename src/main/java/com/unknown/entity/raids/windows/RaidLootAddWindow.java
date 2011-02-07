/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.User;
import com.unknown.entity.dao.*;
import com.unknown.entity.database.*;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidLootListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidLootAddWindow extends Window {

        Raid raid;
        RaidDAO raidDao;
        ItemDAO itemDao;
        CharacterDAO characterDao;
        private List<RaidLootListener> listeners = new ArrayList<RaidLootListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();
        private List<ItemInfoListener> iteminfolisteners = new ArrayList<ItemInfoListener>();
        private Label notice;
        private ComboBox loots;
        private CheckBox heroic;
        private CheckBox upgrade;
        private CheckBox sidegrade;
        private TextField price;
        private ComboBox name;
        private Button addButton;

        RaidLootAddWindow(Raid raid) {
                this.raid = raid;
                this.setCaption(raid.getComment().toString());
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.raidDao = new RaidDB();
                this.itemDao = new ItemDB();
                this.characterDao = new CharacterDB();
                this.setPositionX(600);
                this.setPositionY(300);
                this.setSizeUndefined();
        }

        public void printInfo() throws SQLException {
                HashSet<Items> lootlist = getLootList();
                this.loots = lootListComboBox(lootlist);
                this.heroic = new CheckBox("Heroic");
                this.upgrade = new CheckBox("Upgrade (normal > heroic)");
                this.sidegrade = new CheckBox("Sidegrade (Free)");
                this.price = new TextField("Price");
                this.name = nameComboList();
                this.notice = new Label();
                this.notice.setContentMode(Label.CONTENT_PREFORMATTED);
                this.addButton = new Button("Add");

                addComponent(loots);
                addComponent(heroic);
                addComponent(upgrade);
                addComponent(sidegrade);
                addComponent(price);
                addComponent(name);
                addComponent(notice);
                addComponent(addButton);

                setImmediates();
                setListeners();

        }

        private void setListeners() {
                loots.addListener(new LootChangeListener());
                heroic.addListener(new HeroicChangeListener());
                upgrade.addListener(new UpgradeChangeListener());
                sidegrade.addListener(new SidegradeChangeListener());
                addButton.addListener(new AddRaidListener());
                name.addListener(new NameChangeListener());
        }

        private void setImmediates() {
                price.setImmediate(true);
                heroic.setImmediate(true);
                loots.setImmediate(true);
                upgrade.setImmediate(true);
                sidegrade.setImmediate(true);
                name.setImmediate(true);
        }

        private void addRaidLoot(String name, String loot, boolean isheroic, double price) {
                raidDao.addLootToRaid(raid, name, loot, isheroic, price);
        }

        private ComboBox nameComboList() throws UnsupportedOperationException {
                final ComboBox cname = new ComboBox("Name");
                cname.setWidth("300px");
                HashSet<User> charlist = new HashSet<User>();
                TreeSet<String> sortedlist = new TreeSet<String>();
                charlist.addAll(characterDao.getUsers());
                for (User eachname : charlist) {
                        sortedlist.add(eachname.getUsername());
                }
                for (String s : sortedlist) {
                        cname.addItem(s);
                }
                cname.setNullSelectionAllowed(false);

                return cname;
        }

        private Double getItemPrice(String itemname, Boolean isheroic) {
                Double iPrice = 0.0;
                if (itemname != null) {

                        try {
                                iPrice = getDefaultPrice(itemname, isheroic.booleanValue());
                        } catch (SQLException ex) {
                                Logger.getLogger(RaidLootAddWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                return iPrice;
        }

        private ComboBox lootListComboBox(HashSet<Items> lootlist) throws UnsupportedOperationException {
                ComboBox cloot = new ComboBox("Item");
                cloot.setWidth("300px");
                for (Items eachitem : lootlist) {
                        cloot.addItem(eachitem.getName());
                }
                cloot.setNullSelectionAllowed(false);
                return cloot;
        }

        private Double getDefaultPrice(String itemname, boolean isheroic) throws SQLException {
                return (Double) itemDao.getItemPrice(itemname, isheroic);
        }

        private HashSet<Items> getLootList() {
                HashSet<Items> lootlist = new HashSet<Items>();
                lootlist.addAll(itemDao.getItems());
                return lootlist;
        }

        public void addRaidInfoListener(RaidLootListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (RaidLootListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
                for (CharacterInfoListener charListener : charinfolisteners) {
                        charListener.onCharacterInfoChange();
                }
                for (ItemInfoListener itemListener : iteminfolisteners) {
                        itemListener.onItemInfoChange();
                }
        }

        void addCharacterInfoListener(CharacterInfoListener listener) {
                charinfolisteners.add(listener);
        }

        void addItemInfoListener(ItemInfoListener listener) {
                iteminfolisteners.add(listener);
        }

        private void UpdateNotice() {
                notice.setValue("");
                if (name.getValue() != null && !name.getValue().toString().isEmpty()) {
                        String slot = itemDao.getItemById(itemDao.getItemId(loots.getValue().toString())).getSlot();
                        List<CharacterItem> prev = new ArrayList<CharacterItem>();
                        List<CharacterItem> items = itemDao.getLootForCharacter(name.getValue().toString());
                        for (CharacterItem i : items) {
                                String tempslot = itemDao.getSlotForItemByName(i.getName());
                                if (tempslot.equalsIgnoreCase(slot)) {
                                        prev.add(i);
                                }
                        }
                        if (!prev.isEmpty()) {
                                String temp = name.getValue().toString() + " has already looted: \n";
                                for (CharacterItem s : prev) {
                                        temp += "  " +s.getName();
                                        if (s.getHeroic()) {
                                                temp += " [H] (" + s.getPrice() + " dkp)\n";
                                        } else {
                                                temp += " (" + s.getPrice() + " dkp)\n";
                                        }
                                }
                                temp += "for that slot (" + slot + ").\nIs this an upgrade?";
                                notice.setValue(temp);
                                notice.addStyleName("artifact");
                        }

                }
        }

        private class LootChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                        UpdateNotice();
                }
        }

        private class HeroicChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                }
        }

        private class AddRaidListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addRaidLoot(name.getValue().toString(), loots.getValue().toString(), Boolean.parseBoolean(heroic.getValue().toString()), Double.parseDouble(price.getValue().toString()));
                        notifyListeners();
                        close();
                }
        }

        private class UpgradeChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        heroic.setValue(!heroic.booleanValue());
                        Double upgradePrice = calcUpgradePrice();
                        price.setValue(upgradePrice);
                }

                private double calcUpgradePrice() {
                        if (upgrade.booleanValue()) {
                                return getItemPrice(loots.getValue().toString(), true) - getItemPrice(loots.getValue().toString(), false);
                        } else {
                                return getItemPrice(loots.getValue().toString(), false);
                        }
                }
        }

        private class NameChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        UpdateNotice();
                }
        }

        private class SidegradeChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (sidegrade.booleanValue()) {
                                price.setValue("0.0");
                        } else {
                                price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                        }
                }
        }
}
