
package com.unknown.entity.raids.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.User;
import com.unknown.entity.database.*;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidItem;
import com.unknown.entity.raids.RaidLootListener;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
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


public class RaidLootAddWindow extends Window {

        private HashSet<User> charlist;
        private HashSet<Items> lootlist;
        private Raid raid;
        private List<RaidLootListener> listeners = new ArrayList<RaidLootListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();
        private List<ItemInfoListener> iteminfolisteners = new ArrayList<ItemInfoListener>();
        private Table table;
        private Label notice;
        private ComboBox loots;
        private CheckBox heroic;
        private CheckBox upgrade;
        private CheckBox sidegrade;
        private TextField price;
        private ComboBox name;
        private Button addButton;
        private Button addAllButton;
        private IndexedContainer ic;

        RaidLootAddWindow(Raid raid) {

                this.raid = raid;
                this.setCaption(raid.getComment().toString());
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.setPositionX(600);
                this.setPositionY(300);
                this.setSizeUndefined();
                this.charlist = new HashSet<User>();
                this.charlist.addAll(CharDB.getUsers());
                this.lootlist = new HashSet<Items>();
                this.lootlist.addAll(ItemDB.getItems());
        }

        public void printInfo() {
                
                this.loots = lootListComboBox(lootlist);
                this.heroic = new CheckBox("Heroic");
                this.upgrade = new CheckBox("Upgrade (normal > heroic)");
                this.sidegrade = new CheckBox("Sidegrade (Free)");
                this.price = new TextField();
                this.price.setWidth("200px");
                this.name = nameComboList();
                this.notice = new Label();
                this.notice.setContentMode(Label.CONTENT_PREFORMATTED);
                this.addButton = new Button("Add");
                this.addAllButton = new Button("Add All");
                this.table = new Table("Loots");
                this.ic = new IndexedContainer();
                this.table.setWidth("500px");
                this.table.setHeight("300px");
                
                setHeaders();
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);
                hzl.addComponent(loots);
                hzl.addComponent(name);
                addComponent(hzl);
                
                HorizontalLayout horiz = new HorizontalLayout();
                horiz.setSpacing(true);
                horiz.addComponent(heroic);
                horiz.addComponent(upgrade);
                horiz.addComponent(sidegrade);
                addComponent(horiz);
                
                addComponent(new Label("Price: "));
                HorizontalLayout horizontal = new HorizontalLayout();
                horizontal.setSpacing(true);
                horizontal.addComponent(price);
                horizontal.addComponent(addButton);
                addComponent(horizontal);
                
               addComponent(price);
                addComponent(notice);
               addComponent(addButton);
                addComponent(table);
                addComponent(addAllButton);
                setImmediates();
                setListeners();

        }

        private void setListeners() {
                loots.addListener(new LootChangeListener());
                heroic.addListener(new HeroicChangeListener());
                upgrade.addListener(new UpgradeChangeListener());
                sidegrade.addListener(new SidegradeChangeListener());
                addButton.addListener(new AddLootsListener());
                name.addListener(new NameChangeListener());
                addAllButton.addListener(new AddAllListener());
                table.addListener(new ItemClickListener() {

                        @Override
                        public void itemClick(ItemClickEvent event) {
                                if (event.isCtrlKey()) {
                                        RaidItem item = (RaidItem) event.getItemId();
                                        ic.removeItem(item);
                                }
                        }
                });
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
               
                RaidDB.addLootToRaid(raid, name, loot, isheroic, price);
        }

        private ComboBox nameComboList() throws UnsupportedOperationException {
                final ComboBox cname = new ComboBox("Name");
                cname.setWidth("150px");
                TreeSet<String> sortedlist = new TreeSet<String>();
                for (User eachname : this.charlist) {
                        sortedlist.add(eachname.getUsername());
                }
                for (String s : sortedlist) {
                        cname.addItem(s);
                }
                cname.setNullSelectionAllowed(true);

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
                cloot.setWidth("340px");
                for (Items eachitem : lootlist) {
                        cloot.addItem(eachitem.getName());
                }
                cloot.setNullSelectionAllowed(true);
                return cloot;
        }

        private Double getDefaultPrice(String itemname, boolean isheroic) throws SQLException {
                return (Double) ItemDB.getItemPrice(itemname, isheroic);
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

        private void updateNotice() {
                notice.setValue("");
                if (name.getValue() != null && !name.getValue().toString().isEmpty() && loots.getValue() != null && !loots.getValue().toString().isEmpty()) {
                        String slot = ItemDB.getItemById(ItemDB.getItemId(loots.getValue().toString())).getSlot();
                        List<CharacterItem> prev = new ArrayList<CharacterItem>();
                        List<CharacterItem> items = ItemDB.getLootForCharacter(name.getValue().toString());
                        for (CharacterItem i : items) {
                                String tempslot = ItemDB.getSlotForItemByName(i.getName());
                                if (tempslot.equalsIgnoreCase(slot)) {
                                        prev.add(i);
                                }
                        }
                        if (!prev.isEmpty()) {
                                String temp = name.getValue().toString() + " has already looted: \n";
                                for (CharacterItem s : prev) {
                                        temp += "  " + s.getName();
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

        private void clear() {
                name.setValue(null);
                loots.setValue(null);
                heroic.setValue(false);
                sidegrade.setValue(false);
                upgrade.setValue(false);
                price.setValue("");
        }
        
        private void setHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Name", Label.class, "");
                ic.addContainerProperty("Item", Label.class, "");
                ic.addContainerProperty("Price", Double.class, 0);
                ic.addContainerProperty("Heroic", String.class, "");
                this.table.setContainerDataSource(ic);
                this.table.setColumnWidth("Name", 100);
                this.table.setColumnWidth("Price", 40);
                this.table.setColumnWidth("Heroic", 50);
        }

        private void setNewRow(Item addItem, RaidItem ri) throws ReadOnlyException, ConversionException {
                Label nameLabel = new Label(ri.getLooter());
                String charclass = CharDB.getRoleForCharacter(ri.getLooter()).replace(" ", "").toLowerCase();
                nameLabel.addStyleName(charclass);
                Label itemLabel = new Label(ri.getName());
                itemLabel.addStyleName(ri.getQuality().toLowerCase());
                addItem.getItemProperty("Name").setValue(nameLabel);
                addItem.getItemProperty("Item").setValue(itemLabel);
                addItem.getItemProperty("Price").setValue(ri.getPrice());
                addItem.getItemProperty("Heroic").setValue(ri.isHeroic() ? "Yes" : "No");
        }

        private void addRow() {
                Items i = ItemDB.getSingleItem(loots.getValue().toString());
                RaidItem ri = new RaidItem(i.getName(), name.getValue().toString(), i.getId(), Double.parseDouble(price.getValue().toString()), heroic.booleanValue(), i.getQuality());
                Item addItem = ic.addItem(ri);
                setNewRow(addItem, ri);
        }

        private class LootChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (loots.getValue() != null) {
                                price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                                updateNotice();
                        }
                }
        }

        private class HeroicChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (loots.getValue() != null) {
                                price.setValue(getItemPrice(loots.getValue().toString(), heroic.booleanValue()));
                        }
                }
        }

        private class AddLootsListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addRow();
                        clear();
                }
        }

        private class UpgradeChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (loots.getValue() != null) {
                                heroic.setValue(!heroic.booleanValue());
                                Double upgradePrice = calcUpgradePrice();
                                price.setValue(upgradePrice);
                        }
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
                        updateNotice();
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

        private class AddAllListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        Collection<RaidItem> list = (Collection<RaidItem>) ic.getItemIds();
                        for (RaidItem rl : list) {
                                addRaidLoot(rl.getLooter(), rl.getName(), rl.isHeroic(), rl.getPrice());
                        }
                        notifyListeners();
                        close();
                }
        }
}
