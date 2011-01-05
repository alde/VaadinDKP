/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character.windows;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.Role;
import com.unknown.entity.character.*;
import com.unknown.entity.dao.*;
import com.unknown.entity.database.*;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.items.Items;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidList;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author alde
 */
public class CharacterEditWindow extends Window {

        private final User user;
        private List<CharacterInfoListener> listeners = new ArrayList<CharacterInfoListener>();
        private RaidDAO raidDao;
        private ItemDAO itemDao;
        private CharacterDAO charDao;
        private IndexedContainer ic;
        private Table loots;
        private Application app;
        private ItemList itemList;
        private RaidList raidList;

        public CharacterEditWindow(User user, Application app, RaidList raidList, ItemList itemList) {
                this.user = user;
                this.app = app;
                this.raidList = raidList;
                this.itemList = itemList;
                this.raidDao = new RaidDB();
                this.charDao = new CharacterDB();
                this.itemDao = new ItemDB();
                this.ic = new IndexedContainer();
                this.loots = new Table();
                this.loots.setContainerDataSource(ic);
                this.loots.setEditable(true);
                this.loots.setImmediate(true);
                this.loots.setHeight(150);
                this.setCaption("Edit character: " + user.getUsername());
                this.addStyleName("opaque");
                this.setPositionX(200);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() throws SQLException {
                characterInformation();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterDKP();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterLoots();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterRaids();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                raidsAttended();
        }

        private void characterInformation() {
                final TextField name = new TextField("Name: ", user.getUsername());
                name.setWidth("150px");
                final ComboBox characterClass = characterEditClassComboBox();
                characterClass.setWidth("150px");
                characterClass.addStyleName("select-button");
                String activeStatus = (user.isActive() ? "Active" : "Inactive");
                final CheckBox active = new CheckBox("Status: " + activeStatus);
                active.setValue(user.isActive());
                addComponent(name);
                addComponent(characterClass);
                addComponent(active);

                Button updateButton = characterEditUpdateButton(name, characterClass, active);
                Button deleteButton = new Button("Delete Character");
                deleteButton.addListener(new DeleteButtonClickListener(user));
                HorizontalLayout hzl = new HorizontalLayout();
                Label warning = new Label();
                warning.setWidth("220px");
                warning.setValue("Can NOT be reverted.");
                warning.addStyleName("error");
                hzl.addComponent(updateButton);
                hzl.addComponent(deleteButton);
                hzl.addComponent(warning);
                hzl.setSpacing(true);
                hzl.setMargin(true, false, true, false);
                addComponent(hzl);
        }

        private Button characterEditUpdateButton(final TextField name, final ComboBox characterClass, final CheckBox active) {
                Button updateButton = new Button("Update");
                updateButton.addListener(new updateBtnClickListener(name, characterClass, active));
                return updateButton;
        }

        private ComboBox characterEditClassComboBox() throws ConversionException, UnsupportedOperationException, ReadOnlyException {
                final ComboBox characterClass = new ComboBox("Class: ");
                for (Role role : Role.values()) {
                        characterClass.addItem(role);
                }
                characterClass.setValue(user.getRole());
                characterClass.setNullSelectionAllowed(false);
                return characterClass;
        }

        private void characterLootTableSetColumnHeaders() throws UnsupportedOperationException {
                loots.addContainerProperty("Name", ComboBox.class, "");
                loots.addContainerProperty("Price", Double.class, 0);
                loots.addContainerProperty("Heroic", Boolean.class, false);
                loots.addContainerProperty("Delete", CheckBox.class, false);
        }

        private void characterLootTableSetRow(Item addItem, CharacterItem charitem) throws ReadOnlyException, ConversionException {
                ComboBox items = new ComboBox();
                for (Items item : itemDao.getItems()) {
                        items.addItem(item.getName());
                }
                items.setValue(charitem.getName());
                items.setNullSelectionAllowed(false);
                addItem.getItemProperty("Name").setValue(items);
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
                addItem.getItemProperty("Heroic").setValue(charitem.getHeroic());
                addItem.getItemProperty("Delete").setValue("");
        }

        private int updateCharacter(String name, String charclass, boolean active) {
                return charDao.updateCharacter(user, name, charclass, active);
        }

        private void characterLoots() {
                Button update = new Button("Update");
                lootList();
                update.addListener(new updateLootsListener());
                addComponent(new Label("Loots"));
                if (loots.size() > 0) {
                        loots.addStyleName("striped");
                        addComponent(loots);
                        addComponent(update);
                        loots.addListener(new LootClickListener());
                } else {
                        addComponent(new Label("No items looted yet."));
                }
        }

        private void characterRaids() throws SQLException {
                Table raids = raidList(user);
                addComponent(new Label("Raids"));
                if (raids.size() > 0) {
                        raids.addStyleName("striped");
                        raids.setWidth("220px");
                        addComponent(raids);
                        raids.addListener(new RaidListClickListener());
                } else {
                        addComponent(new Label("No raids attended yet."));
                }
        }

        private Table raidList(User user) throws SQLException {
                Table tbl = new Table();
                tbl.addContainerProperty("Comment", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
                tbl.setHeight("150px");
                for (Raid charraid : raidDao.getRaidsForCharacter(user.getId())) {
                        Item addItem = tbl.addItem(charraid);
                        addItem.getItemProperty("Comment").setValue(charraid.getComment());
                        addItem.getItemProperty("Date").setValue(charraid.getDate());
                }
                return tbl;
        }

        private void characterDKP() throws OutOfBoundsException, OverlapsException {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Shares: "));
                Label shares = new Label("" + user.getShares());
                shares.addStyleName("color");
                hzl.addComponent(shares);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Earned: "));
                Label dkpearned = new Label("" + user.getDKPEarned());
                dkpearned.addStyleName("color");
                hzl.addComponent(dkpearned);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Spent: "));
                Label dkpspent = new Label("" + user.getDKPSpent());
                dkpspent.addStyleName("color");
                hzl.addComponent(dkpspent);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP: "));
                Label dkp = new Label("" + user.getDKP());
                if (user.getDKP() >= 0) {
                        dkp.addStyleName("positive");
                } else {
                        dkp.addStyleName("negative");
                }
                hzl.addComponent(dkp);
                addComponent(hzl);
        }

        private void lootList() {
                characterLootTableSetColumnHeaders();
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = ic.addItem(charitem);
                        characterLootTableSetRow(addItem, charitem);
                }
        }

        public void addCharacterInfoListener(CharacterInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (CharacterInfoListener characterListener : listeners) {
                        characterListener.onCharacterInfoChange();
                }
        }

        private void removeLootFromCharacter(Item item) {
                charDao.removeLootFromCharacter(item.getItemProperty("Name").toString(), user);
        }

        private void updateLootForCharacter(Item item, int lootid) {
                charDao.updateLootForCharacter(item.getItemProperty("Name").toString(), Double.parseDouble(item.getItemProperty("Price").toString()), Boolean.parseBoolean(item.getItemProperty("Heroic").toString()), user, lootid);
        }

        private class updateBtnClickListener implements ClickListener {

                private final TextField name;
                private final ComboBox characterClass;
                private final CheckBox active;

                public updateBtnClickListener(TextField name, ComboBox characterClass, CheckBox active) {
                        this.name = name;
                        this.characterClass = characterClass;
                        this.active = active;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        int success = updateCharacter(name.getValue().toString(), characterClass.getValue().toString(), (Boolean) active.getValue());
                        notifyListeners();
                        close();
                }
        }

        private class DeleteButtonClickListener implements ClickListener {

                final User user;

                public DeleteButtonClickListener(User user) {
                        this.user = user;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        charDao.deleteCharacter(user);
                        notifyListeners();
                        close();
                }
        }

        private void raidsAttended() {
                String attendance = charDao.getAttendanceRaids(user);
                Label attended = new Label();
                attended.setValue("Attended " + attendance + "% of raids the last 30 days.");
                addComponent(attended);
        }

        private void doUpdateLoots() {
                for (Iterator i = loots.getItemIds().iterator(); i.hasNext();) {
                        CharacterItem iid = (CharacterItem) i.next();
                        Item item = loots.getItem(iid);
                        if (item.getItemProperty("Delete").toString().equalsIgnoreCase("True")) {
                                removeLootFromCharacter(item);
                        } else {
                                updateLootForCharacter(item, iid.getId());
                        }

                }
        }

        private class updateLootsListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        doUpdateLoots();
                        notifyListeners();
                        close();
                }
        }

        private class LootClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                                ItemDAO itemDao = new ItemDB();
                                CharacterItem citem = (CharacterItem) event.getItemId();
                                Items temp = itemDao.getSingleItem(citem.getName());
                                PopUpControl pop = new PopUpControl(app);
                                pop.setItemList(itemList);
                                pop.showProperItemWindow(temp);
                }
        }

        private class RaidListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                                Raid item = (Raid) event.getItemId();
                                PopUpControl pop = new PopUpControl(app);
                                pop.setItemList(itemList);
                                pop.setRaidList(raidList);
                                pop.showProperRaidWindow(item);
                }
        }
}
