/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character.windows;

import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.Role;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.User;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.Raid;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.IndexedContainer;
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
import com.vaadin.ui.VerticalLayout;
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
        private IndexedContainer ic;
        private Table loots;

        public CharacterEditWindow(User user) {
                this.user = user;
                this.raidDao = new RaidDB();
                this.ic = new IndexedContainer();
                this.loots.setContainerDataSource(ic);
                this.loots = lootList(user);
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
                characterDKP();
                characterLoots();
                characterRaids();
                raidsAttended();
        }

        private void characterInformation() {
                addComponent(new Label("Character information"));

                final TextField name = new TextField("Name: ", user.getUsername());
                final ComboBox characterClass = characterEditClassComboBox();
                final CheckBox active = new CheckBox("Status: ", user.isActive());
                addComponent(name);
                addComponent(characterClass);
                addComponent(active);

                Button updateButton = characterEditUpdateButton(name, characterClass, active);
                Button deleteButton = new Button("Delete Character");
                deleteButton.addListener(new DeleteButtonClickListener());
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
                ic.addContainerProperty("Name", String.class, "");
                ic.addContainerProperty("Price", Double.class, 0);
                ic.addContainerProperty("Delete", CheckBox.class, false);
        }

        private void characterLootTableSetRow(Item addItem, CharacterItem charitem) throws ReadOnlyException, ConversionException {
                System.out.println(charitem.getName());
                System.out.println(addItem);
                System.out.println(addItem.getItemProperty("Name").toString());
                System.out.println(charitem.getPrice()+"");
                System.out.println(addItem.getItemProperty("Price").toString());
                System.out.println(addItem.getItemProperty("Delete").toString());
                addItem.getItemProperty("Name").setValue(charitem.getName());
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
                addItem.getItemProperty("Delete").setValue("");
        }

        private int updateCharacter(String name, String charclass, boolean active) {
                CharacterDAO charDao = new CharacterDB();
                return charDao.updateCharacter(user, name, charclass, active);
        }

        private void characterLoots() {
                Button update = new Button("Update");
                update.addListener(new updateLootsListener());
                addComponent(new Label("Loots"));
                if (loots.size() > 0) {
                        addComponent(lootList(user));
                        addComponent(update);
                } else {
                        addComponent(new Label("No items looted yet."));
                }


        }

        private void characterRaids() throws SQLException {
                Table raids = raidList(user);
                addComponent(new Label("Raids"));
                if (raids.size() > 0) {
                        addComponent(raidList(user));
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
                addComponent(new Label("DKP"));

                VerticalLayout vert = new VerticalLayout();
                vert.addComponent(new Label("Shares: " + user.getShares()));
                vert.addComponent(new Label("DKP Earned: " + user.getDKPEarned()));
                vert.addComponent(new Label("DKP Spent: " + user.getDKPSpent()));
                vert.addComponent(new Label("DKP: " + user.getDKP()));
                addComponent(vert);
        }

        private Table lootList(User user) {
                characterLootTableSetColumnHeaders();
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = ic.addItem(charitem);
                        System.out.println(addItem);
                        characterLootTableSetRow(addItem, charitem);
                }
                return loots;

        }

        public void addCharacterInfoListener(CharacterInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (CharacterInfoListener characterListener : listeners) {
                        characterListener.onCharacterInfoChange();
                }
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
                        System.out.println("" + success);
                        notifyListeners();
                        close();
                }
        }

        private static class DeleteButtonClickListener implements ClickListener {

                public DeleteButtonClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        throw new UnsupportedOperationException("Not supported yet.");
                }
        }

        private void raidsAttended() {
                CharacterDAO charDao = new CharacterDB();
                String attendance = charDao.getAttendanceRaids(user);
                Label attended = new Label();
                attended.setValue("Attended " + attendance + "% of raids the last 30 days.");
                addComponent(attended);
        }
        private void doUpdateLoots() {
                for (Iterator i = ic.getItemIds().iterator(); i.hasNext();) {
                        Item ci = ic.getItem(i);
                        System.out.println(ci.getItemProperty("name") + " -- " + ci.getItemProperty("price"));
                }
        }

        private class updateLootsListener implements ClickListener {
                
                public updateLootsListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        doUpdateLoots();
                }
        }
}
