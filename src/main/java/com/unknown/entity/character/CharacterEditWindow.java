/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.unknown.entity.character;

import com.unknown.entity.Role;
import com.vaadin.data.Item;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 *
 * @author alde
 */
public class CharacterEditWindow extends Window {
        private final User user;

        public CharacterEditWindow(User user) {
                this.user = user;
                this.setCaption(user.getUsername());
                this.center();
                this.setWidth("400px");
                this.setHeight("400px");
        }
        public void printInfo() {
                CharacterInformation();
                CharacterDKP();
                CharacterLoots();

        }

        private void CharacterInformation() {
                addComponent(new Label("Character information"));
                TextField name = new TextField("Name: ",user.getUsername());
                ComboBox characterClass = new ComboBox("Class: ");
                for (Role role : Role.values()) {
                        characterClass.addItem(role);
                }
                characterClass.setValue(user.getRole());
                characterClass.setNullSelectionAllowed(false);
                CheckBox active = new CheckBox("Status: ", user.isActive());
                addComponent(name);
                addComponent(characterClass);
                addComponent(active);
        }

        private void CharacterLoots() {
                Table loots = lootList(user);
                if (loots.size() > 0) {
                        addComponent(lootList(user));
                } else {
                        addComponent(new Label("No items looted yet."));
                }
        }

        private void CharacterDKP() throws OutOfBoundsException, OverlapsException {
                addComponent(new Label("DKP"));
                GridLayout dkpGrid = new GridLayout(2, 4);
                dkpGrid.addComponent(new Label("Shares: "), 0, 0);
                dkpGrid.addComponent(new Label("DKP Earned: "), 0, 1);
                dkpGrid.addComponent(new Label("DKP Spent: "), 0, 2);
                dkpGrid.addComponent(new Label("DKP: "), 0, 3);
                dkpGrid.addComponent(new Label("" + user.getShares()), 1, 0);
                dkpGrid.addComponent(new Label("" + user.getDKPEarned()), 1, 1);
                dkpGrid.addComponent(new Label("" + user.getDKPSpent()), 1, 2);
                dkpGrid.addComponent(new Label("" + user.getDKP()), 1, 3);
                addComponent(dkpGrid);
        }

        private Table lootList(User user) {
                Table tbl = new Table();
                tbl.addContainerProperty("Name", String.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.setHeight(150);
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = tbl.addItem(charitem.getId());
                        addItem.getItemProperty("Name").setValue(charitem.getName());
                        addItem.getItemProperty("Price").setValue(charitem.getPrice());
                }
                return tbl;

        }
}
