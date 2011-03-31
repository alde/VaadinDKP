/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.Armor;
import com.unknown.entity.PopUpControl;
import com.unknown.entity.database.CharDB;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.raids.RaidList;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ItemSorter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bobo
 */
public class DkpList extends Table implements CharacterInfoListener {

        private IndexedContainer ic;
        private DkpList dkpList = this;
        private CharacterList charList;
        private ItemList itemList;
        private RaidList raidList;
        private Application app;

        public DkpList(Application app) {
                this.ic = new IndexedContainer();
                this.app = app;
                this.setSelectable(true);
                this.setWidth("185px");
                this.setHeight("650px");
                this.setPageLength(0);
                this.addListener(new dkpListClickListener());
                dkpListSetColumnHeaders();
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
                ic.addContainerProperty("Attendance", Label.class, 0);
                ic.setItemSorter(new DefaultItemSorter(new DkpItemSorter()));
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        @Override
        public void onCharacterInfoChange() {
                CharDB.clearCache();
                update();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        public void printList() {
                clear();
                List<User> users = CharDB.getUsersSortedByDKP();
                for (final User user : users) {
                        if (user.isActive()) {
                                Item addItem = addItem(user);
                                dkpListAddRow(addItem, user);
                        }
                }
                this.setColumnCollapsingAllowed(true);
                this.setColumnCollapsed("Armor", true);
                this.setColumnCollapsed("Attendance", true);

        }

        private void dkpListAddRow(Item addItem, final User user) throws ConversionException, ReadOnlyException {
                Label charname = new Label(user.getUsername());
                charname.addStyleName(user.getRole().toString().replace(" ", "").toLowerCase());
                addItem.getItemProperty("Name").setValue(charname);
                addItem.getItemProperty("Armor").setValue(user.getArmor());
                Label dkp = new Label("" + user.getDKP());
                dkp.setContentMode(Label.CONTENT_XHTML);
                if (user.getDKP() >= 0) {
                        dkp.addStyleName("positive");
                } else {
                        dkp.addStyleName("negative");
                }
                addItem.getItemProperty("DKP").setValue(dkp);
                Label att = new Label(""+user.getAttendance());
                Double attendance = user.getAttendance();
                if (attendance >= 0 && attendance < 50) {
                        att.setStyleName("negative");
                } else if (attendance > 50 && attendance < 65) {
                        att.setStyleName("uncommon");
                } else if (attendance >= 65 && attendance < 75) {
                        att.setStyleName("rare");
                } else if (attendance >= 75 && attendance < 90) {
                        att.setStyleName("epic");
                } else if (attendance >= 90) {
                        att.setStyleName("legendary");
                }
                addItem.getItemProperty("Attendance").setValue(att);


        }

        public void filter(Object value) {
                ic.removeAllContainerFilters();
                String foo = filterString(value);
                ic.addContainerFilter("Armor", foo, true, false);
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

        class DkpItemSorter implements Comparator<Object> {

                public boolean isParsableToDouble(String i) {
                        try {
                                Double.parseDouble(i);
                                return true;
                        } catch (NumberFormatException nfe) {
                                return false;
                        }
                }

                @Override
                public int compare(Object o1, Object o2) {
                        if (o1 instanceof Label && o2 instanceof Label) {
                                String s1 = ((Label) o1).getValue().toString();
                                String s2 = ((Label) o2).getValue().toString();
                                if (isParsableToDouble(s1) && isParsableToDouble(s2)) {
                                        return Double.parseDouble(s1) < Double.parseDouble(s2) ? 1 : 0;
                                }
                                return s1.compareToIgnoreCase(s2);
                        } else if (o1 instanceof Double && o2 instanceof Double) {
                                return (Double) o1 < (Double) o2 ? 1 : 0;
                        } else {
                                return 0;
                        }
                }
        }
}
