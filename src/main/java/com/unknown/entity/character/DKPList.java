/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.Armor;
import com.unknown.entity.User;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bobo
 */
public class DKPList extends Table {

        private CharacterDAO characterDAO;
        IndexedContainer ic = new IndexedContainer();

        public DKPList(CharacterDAO characterDAO) {
                this.characterDAO = characterDAO;

                ic.addContainerProperty("Name", String.class, "");
                ic.addContainerProperty("Armor", Armor.class, "");
                this.setContainerDataSource(ic);
                this.setColumnCollapsingAllowed(true);
                try {
                        this.setColumnCollapsed("Armor", true);
                } catch (IllegalAccessException ex) {
                        Logger.getLogger(DKPList.class.getName()).log(Level.SEVERE, null, ex);
                }
                addContainerProperty("DKP", Double.class, 0);
                this.setWidth("180px");
                this.setHeight("500px");
                this.addListener(new ItemClickListener() {

                        @Override
                        public void itemClick(ItemClickEvent event) {
                                User user = (User) event.getItemId();
                                CharacterInfoWindow info = new CharacterInfoWindow(user);
                                info.printInfo();
                                info.setCaption(user.getUsername());
                                getApplication().getMainWindow().addWindow(info);
                                info.center();
                                info.setWidth("400px");
                                info.setHeight("400px");
                        }
                });
        }

        public void clear() {
                this.removeAllItems();
        }

        public void printList() {
                clear();
                List<User> users = characterDAO.getUsers();
                Collections.sort(users, new Comparator<User>() {

                        @Override
                        public int compare(User t, User t1) {
                                return t.getDKP() < t1.getDKP() ? 1 : 0;
                        }
                });
                for (final User user : users) {
                        Item addItem = addItem(user);
                        addItem.getItemProperty("Name").setValue(user.getUsername());
                        addItem.getItemProperty("Armor").setValue(user.getArmor());
                        addItem.getItemProperty("DKP").setValue(user.getDKP());

                }

        }

        public void filter(Object value) {
                String foo;
                if (value == null) {
                        foo = "";
                } else {
                        foo = value.toString();
                }
                ic.removeAllContainerFilters();
                ic.addContainerFilter("Armor", foo, true, false);
        }
}
