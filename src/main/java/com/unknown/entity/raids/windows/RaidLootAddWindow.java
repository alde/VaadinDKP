/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidChar;
import com.unknown.entity.raids.RaidInfoListener;
import com.unknown.entity.raids.RaidLootListener;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
        }

        public void printInfo() throws SQLException {
//                final ComboBox boss = bossListComboBox();
                HashSet<Items> lootlist = getLootList();
                final ComboBox loots = lootListComboBox(lootlist);
                final CheckBox heroic = new CheckBox("Heroic");
                final TextField price = new TextField("Price");
                final ComboBox name = nameComboList();
                final Button addButton = new Button("Add");

//                addComponent(boss);
                addComponent(loots);
                addComponent(heroic);
                addComponent(price);
                addComponent(name);
                addComponent(addButton);

                setImmediates(price, heroic, loots,name);
                setListeners(loots, price, heroic, addButton,name);

        }

        private void setListeners(final ComboBox loots, final TextField price, final CheckBox heroic, final Button addButton, final ComboBox name) {
                loots.addListener(new LootChangeListener(price, loots, heroic));
                heroic.addListener(new HeroicChangeListener(price, loots, heroic));
                addButton.addListener(new AddRaidListener(name, loots, heroic, price));
        }

        private void setImmediates(final TextField price, final CheckBox heroic, final ComboBox loots, final ComboBox name) {
                price.setImmediate(true);
                heroic.setImmediate(true);
                loots.setImmediate(true);
//                boss.setImmediate(true);
                name.setImmediate(true);
        }

        private void addRaidLoot(String name, String loot, boolean isheroic, double price) {
                try {
                        raidDao.addLootToRaid(raid, name, loot, isheroic, price);
                } catch (SQLException ex) {
                        Logger.getLogger(RaidLootAddWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private ComboBox nameComboList() throws UnsupportedOperationException {
                final ComboBox name = new ComboBox("Name");
                name.setWidth("300px");
                name.addStyleName("select-button");
                HashSet<RaidChar> charlist = new HashSet<RaidChar>();
                TreeSet<String> sortedlist = new TreeSet<String>();
                charlist.addAll(raid.getRaidChars());
                for (RaidChar eachname : charlist) {
                        sortedlist.add(eachname.getName());
                }
                for (String s : sortedlist) {
                        name.addItem(s);
                }
                name.setNullSelectionAllowed(false);

                return name;
        }

        private Double getItemPrice(String itemname, Boolean isheroic) {
                Double price = 0.0;
                if (itemname != null) {

                        try {
                                price = getDefaultPrice(itemname, isheroic.booleanValue());
                        } catch (SQLException ex) {
                                Logger.getLogger(RaidLootAddWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                return price;
        }

        private ComboBox lootListComboBox(HashSet<Items> lootlist) throws UnsupportedOperationException {
                ComboBox loots = new ComboBox("Item");
                loots.setWidth("300px");
                loots.addStyleName("select-button");
                for (Items eachitem : lootlist) {
                        loots.addItem(eachitem.getName());
                }
                loots.setNullSelectionAllowed(false);
                return loots;
        }

        private Double getDefaultPrice(String itemname, boolean isheroic) throws SQLException {
                return (Double) itemDao.getItemPrice(itemname, isheroic);
        }

//        private ComboBox bossListComboBox() throws ConversionException, ReadOnlyException, UnsupportedOperationException, SQLException {
//                List<String> bosslist = new ArrayList<String>();
//                bosslist = raidDao.getBossesForRaid(raid);
//                ComboBox boss = new ComboBox();
//                for (String eachboss : bosslist) {
//                        boss.addItem(eachboss);
//                }
//                boss.setNullSelectionAllowed(false);
//                Collection<?> itemIds = boss.getItemIds();
//                boss.setValue(itemIds.iterator().next());
//                return boss;
//        }

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
        }

        void addCharacterInfoListener(CharacterInfoListener listener) {
                charinfolisteners.add(listener);
        }

        private class LootChangeListener implements ValueChangeListener {

                private final TextField price;
                private final ComboBox loots;
                private final CheckBox heroic;

                public LootChangeListener(TextField price, ComboBox loots, CheckBox heroic) {
                        this.price = price;
                        this.loots = loots;
                        this.heroic = heroic;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                }
        }

        private class HeroicChangeListener implements ValueChangeListener {

                private final TextField price;
                private final ComboBox loots;
                private final CheckBox heroic;

                public HeroicChangeListener(TextField price, ComboBox loots, CheckBox heroic) {
                        this.price = price;
                        this.loots = loots;
                        this.heroic = heroic;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                }
        }

        private class AddRaidListener implements ClickListener {

                private final ComboBox name;
                private final ComboBox loots;
                private final CheckBox heroic;
                private final TextField price;

                public AddRaidListener(ComboBox name, ComboBox loots, CheckBox heroic, TextField price) {
            
                        this.name = name;
                        this.loots = loots;
                        this.heroic = heroic;
                        this.price = price;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        addRaidLoot(name.getValue().toString(), loots.getValue().toString(), Boolean.parseBoolean(heroic.getValue().toString()), Double.parseDouble(price.getValue().toString()));
                        notifyListeners();
                }
        }
}
