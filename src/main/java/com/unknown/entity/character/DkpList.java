/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.Armor;
import com.unknown.entity.PopUpControl;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.raids.RaidList;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bobo
 */
public class DkpList extends Table implements CharacterInfoListener {

        private CharacterDAO charDao;
        private IndexedContainer ic;
        private DkpList dkpList = this;
        private CharacterList charList;
        private Application app;
        private ItemList itemList;
        private RaidList raidList;

        public DkpList(CharacterDAO characterDAO, Application app) {
                this.charDao = characterDAO;
                this.ic = new IndexedContainer();
                this.app = app;
                dkpListSetColumnHeaders();
                this.setSelectable(true);
                this.setWidth("185px");
                this.setHeight("650px");
                this.setPageLength(0);

                this.addListener(new dkpListClickListener());
        }

        public void setLists(ItemList itemList, RaidList raidList) {
                this.itemList = itemList;
                this.raidList = raidList;
        }

        @SuppressWarnings("CallToThreadDumpStack")
        private void dkpListSetColumnHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Name", Label.class, "");
                ic.addContainerProperty("Armor", Armor.class, "");
                ic.addContainerProperty("DKP", Label.class, 0);
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        @Override
        public void onCharacterInfoChange() {
                charDao.clearCache();
                update();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        public void printList() {
                clear();
                List<User> users = charDao.getUsers();
                Collections.sort(users, new Comparator<User>() {

                        @Override
                        public int compare(User t, User t1) {
                                return t.getDKP() < t1.getDKP() ? 1 : 0;
                        }
                });
                for (final User user : users) {
                        if (user.isActive()) {
                                Item addItem = addItem(user);
                                dkpListAddRow(addItem, user);
                        }
                }
                this.setColumnCollapsingAllowed(true);
                this.setColumnCollapsed("Armor", true);

        }

        private void dkpListAddRow(Item addItem, final User user) throws ConversionException, ReadOnlyException {
                Label charname = new Label(user.getUsername());
                charname.addStyleName(user.getRole().toString().replace(" ", "").toLowerCase());
                addItem.getItemProperty("Name").setValue(charname);
                addItem.getItemProperty("Armor").setValue(user.getArmor());
                Label dkp = new Label("" + user.getDKP());
                if (user.getDKP() >= 0) {
                        dkp.addStyleName("positive");
                } else {
                        dkp.addStyleName("negative");
                }
                addItem.getItemProperty("DKP").setValue(dkp);
        }

        public void filter(Object value) {
                ic.removeAllContainerFilters();
                ic.addContainerFilter("Armor", filterString(value), true, false);
        }

        private String filterString(Object value) {
                if (value == null) {
                        return "";
                } else {
                        return value.toString();
                }
        }

        public void setCharacterList(CharacterList charList) {
                this.charList = charList;
        }

        private class dkpListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        User user = (User) event.getItemId();
                        PopUpControl pop = new PopUpControl(DkpList.this.getApplication());
                        pop.setItemList(itemList);
                        pop.setRaidList(raidList);
                        pop.setDkpList(dkpList);
                        pop.setCharacterList(charList);
                        pop.showProperCharWindow(user);
                }
        }
}
