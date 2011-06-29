
package com.unknown.entity.character.windows;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.Role;
import com.unknown.entity.character.*;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class CharacterEditWindow extends Window implements CharacterInfoListener {

        private User user;
        private List<CharacterInfoListener> listeners = new ArrayList<CharacterInfoListener>();
        private IndexedContainer ic;
        private Table loots;
        private Application app;
        private ItemList itemList;
        private DkpList dkpList;
        private RaidList raidList;
        private Label shares;
        private Label noPun;
        private Table punishments;
        private CharacterEditWindow charInfo = this;
        private HorizontalLayout hraid;
        private TextField nameField;

        public CharacterEditWindow(User user, Application app, RaidList raidList, ItemList itemList, DkpList dkpList) {
                this.user = user;
                this.app = app;
                this.raidList = raidList;
                this.itemList = itemList;
                this.dkpList = dkpList;
                this.ic = new IndexedContainer();
                this.loots = new Table();
                this.loots.setContainerDataSource(ic);
                this.punishments = new AdjustmentTable(user);
                this.loots.setEditable(true);
                this.loots.setImmediate(true);
                this.loots.setHeight("400px");
                this.setCaption("Edit character: " + user.getUsername());
                this.addStyleName("opaque");
                this.setPositionX(60);
                this.setPositionY(50);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() throws SQLException {
                characterInformation();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterLoots();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterRaids();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                raidsAttended();
        }

        private void addRaidListRow(Item addItem, Raid charraid) throws ConversionException, ReadOnlyException {
                addItem.getItemProperty("Comment").setValue(charraid.getComment());
                addItem.getItemProperty("Date").setValue(charraid.getDate());
        }

        private void characterInformation() {
                nameField = new TextField("Name: ", user.getUsername());
                nameField.setWidth("150px");
                nameField.setImmediate(true);
                final ComboBox characterClass = characterEditClassComboBox();
                characterClass.setWidth("150px");
                characterClass.addStyleName("select-button");
                String activeStatus = (user.isActive() ? "Active" : "Inactive");
                final CheckBox active = new CheckBox("Status: " + activeStatus);
                active.setValue(user.isActive());
                HorizontalLayout hzl = new HorizontalLayout();
                VerticalLayout vrt1 = new VerticalLayout();
                vrt1.addComponent(nameField);
                vrt1.addComponent(characterClass);
                vrt1.addComponent(active);
                hzl.addComponent(vrt1);

                VerticalLayout vrt2 = new VerticalLayout();
                Button updateButton = characterEditUpdateButton(nameField, characterClass, active);
                Button deleteButton = new Button("Delete Character");
                deleteButton.addListener(new DeleteButtonClickListener(user));
                Label warning = new Label();
                warning.setWidth("220px");
                warning.setValue("Can NOT be reverted.");
                warning.addStyleName("error");
                vrt2.addComponent(updateButton);
                vrt2.addComponent(deleteButton);
                vrt2.addComponent(warning);
                vrt2.setSpacing(true);
                hzl.addComponent(vrt2);

                GridLayout gl = new GridLayout(2, 4);
                gl.addComponent(new Label("Shares: "), 0, 0);
                this.shares = new Label("" + user.getShares());
                shares.addStyleName("color");
                gl.addComponent(shares, 1, 0);

                gl.addComponent(new Label("DKP Earned: "), 0, 1);
                Label dkpearned = new Label("" + user.getDKPEarned());
                dkpearned.addStyleName("color");
                gl.addComponent(dkpearned, 1, 1);

                gl.addComponent(new Label("DKP Spent: "), 0, 2);
                Label dkpspent = new Label("" + user.getDKPSpent());
                dkpspent.addStyleName("color");
                gl.addComponent(dkpspent, 1, 2);

                gl.addComponent(new Label("DKP: "), 0, 3);
                Label dkp = new Label("" + user.getDKP());
                if (user.getDKP() >= 0) {
                        dkp.addStyleName("positive");
                } else {
                        dkp.addStyleName("negative");
                }
                gl.addComponent(dkp, 1, 3);
                hzl.addComponent(gl);
                Button addPunishmentButton = new Button("Add Adjustment");
                addPunishmentButton.addListener(new AddAdjustmentListener());
                hzl.addComponent(addPunishmentButton);
                gl.setSpacing(true);
                hzl.setSpacing(true);
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
                loots.addContainerProperty("Slot", String.class, "");
                loots.addContainerProperty("Heroic", Boolean.class, false);
                loots.addContainerProperty("Delete", CheckBox.class, false);
        }

        private void characterLootTableSetRow(Item addItem, CharacterItem charitem) throws ReadOnlyException, ConversionException {
                ComboBox items = new ComboBox();
                for (Items item : ItemDB.getItems()) {
                        items.addItem(item.getName());
                }
                items.setValue(charitem.getName());
                items.setNullSelectionAllowed(false);
                String slot = ItemDB.getSlotForItemByName(charitem.getName());
                addItem.getItemProperty("Name").setValue(items);
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
                addItem.getItemProperty("Slot").setValue(slot);
                addItem.getItemProperty("Heroic").setValue(charitem.getHeroic());
                addItem.getItemProperty("Delete").setValue("");
        }

        private void setRaidListHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Comment", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
        }

        private void updateCharacter(String name, String charclass, boolean active) {
                CharDB.updateCharacter(user, name, charclass, active);
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

        private void characterRaids() {
                hraid = new HorizontalLayout();
                hraid.addComponent(raiTable());
                hraid.addComponent(punTable());
                addComponent(hraid);
        }
        private Component raiTable() {
                Table raids = raidList(user);
                raids.setCaption("Raids");
                if (raids.size() > 0) {
                        raids.addStyleName("striped");
                        raids.addListener(new RaidListClickListener());
                        return raids;
                } else {
                        return new Label("No raids attended yet.");
                }
        }

        private Component punTable() {
                punishments.setCaption("Adjustments");
                if (punishments.size() > 0) {
                        punishments.addStyleName("striped");
                        punishments.addListener(new AdjustmentClickListener());
                        return punishments;

                } else {
                        noPun = new Label("No adjustments.");
                        return noPun;
                }
        }

        private Table raidList(User user) {
                Table tbl = new Table();
                setRaidListHeaders(tbl);
                tbl.setHeight("200px");
                List<Raid> cRaids = RaidDB.getRaidsForCharacter(user.getId());
                Collections.sort(cRaids, new CompareDates());
                for (Raid charraid : cRaids) {
                        Item addItem = tbl.addItem(charraid);
                        addRaidListRow(addItem, charraid);
                }
                if (tbl.getWidth() < 300) {
                        tbl.setWidth("300px");
                }
                return tbl;
        }

        private void lootList() {
                characterLootTableSetColumnHeaders();
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = ic.addItem(charitem);
                        characterLootTableSetRow(addItem, charitem);
                }
        }

        public final void addCharacterInfoListener(CharacterInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (CharacterInfoListener characterListener : listeners) {
                        characterListener.onCharacterInfoChange();
                }
        }

        private void removeLootFromCharacter(Item item) {
                CharDB.removeLootFromCharacter(item.getItemProperty("Name").toString(), user);
        }

        private void updateLootForCharacter(Item item, int lootid) {
                CharDB.updateLootForCharacter(item.getItemProperty("Name").toString(), Double.parseDouble(item.getItemProperty("Price").toString()), Boolean.parseBoolean(item.getItemProperty("Heroic").toString()), user, lootid);
        }

        public void update() {
                this.user = CharDB.getUser(user.getUsername());
                shares.setValue("");
                hraid.removeAllComponents();
                shares.setValue(user.getShares());
                punishments = new AdjustmentTable(user);
                hraid.addComponent(raiTable());
                hraid.addComponent(punTable());
        }

        @Override
        public void onCharacterInfoChange() {
                update();
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
                        updateCharacter(name.getValue().toString(), characterClass.getValue().toString(), (Boolean) active.getValue());
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
                        CharDB.deleteCharacter(user);
                        notifyListeners();
                        close();
                }
        }

        private void raidsAttended() {
                Double attendance = user.getAttendance();
                
                Label attended = new Label();
                attended.setValue("Attended " + attendance + "% of raids the last 60 days.");
                if (attendance >= 0 && attendance < 50) {
                        attended.setStyleName("negative");
                } else if (attendance > 50 && attendance < 65) {
                        attended.setStyleName("uncommon");
                } else if (attendance >= 65 && attendance < 75) {
                        attended.setStyleName("rare");
                } else if (attendance >= 75 && attendance < 90) {
                        attended.setStyleName("epic");
                } else if (attendance >= 90) {
                        attended.setStyleName("legendary");
                }
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
                        CharacterItem citem = (CharacterItem) event.getItemId();
                        Items temp = ItemDB.getSingleItem(citem.getName());
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

        private class CompareDates implements Comparator<Raid> {

                @Override
                public int compare(Raid t, Raid t1) {
                        return t1.getDate().compareTo(t.getDate());
                }
        }

        private class AddAdjustmentListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        AddAdjustmentWindow addItem = new AddAdjustmentWindow(user.getId());
                        addItem.addCharacterInfoListener(dkpList);
                        addItem.addCharacterInfoListener(charInfo);
                        addItem.addApplication(app);
                        addItem.printInfo();
                        app.getMainWindow().addWindow(addItem);

                }
        }

        private class AdjustmentClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isCtrlKey() && event.isShiftKey()) {
                                Adjustment p = (Adjustment) event.getItemId();
                               
                                RaidDB.removeAdjustment(p);
                                notifyListeners();
                                update();
                        }
                }
        }
}
