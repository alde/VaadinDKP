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
import java.util.List;

/**
 *
 * @author alde
 */
public class CharacterEditWindow extends Window {

        private final User user;
        private List<CharacterInfoListener> listeners = new ArrayList<CharacterInfoListener>();
        private RaidDAO raidDao;

        public CharacterEditWindow(User user) {
                this.user = user;
                this.raidDao = new RaidDB();
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

        private void characterLootTableSetColumnHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", String.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
        }

        private void characterLootTableSetRow(Item addItem, CharacterItem charitem) throws ReadOnlyException, ConversionException {
                addItem.getItemProperty("Name").setValue(charitem.getName());
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
        }

        private int updateCharacter(String name, String charclass, boolean active) {
                CharacterDAO charDao = new CharacterDB();
                return charDao.updateCharacter(user, name, charclass, active);
        }

        private void characterLoots() {
                Table loots = lootList(user);
                addComponent(new Label("Loots"));
                if (loots.size() > 0) {
                        addComponent(lootList(user));
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
                Table tbl = new Table();
                characterLootTableSetColumnHeaders(tbl);
                tbl.setHeight(150);
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = tbl.addItem(charitem.getId());
                        characterLootTableSetRow(addItem, charitem);
                }
                return tbl;

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
}
