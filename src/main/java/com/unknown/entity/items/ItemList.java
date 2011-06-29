
package com.unknown.entity.items;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.raids.RaidList;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ItemList extends Table implements ItemInfoListener {
        private IndexedContainer ic;
        private final ItemList itemList = this;
        private CharacterList charLiist;
        private RaidList raidList;
        private DkpList dkpList;
        private int longest;

        public ItemList() {
                this.ic = new IndexedContainer();
                this.setSizeUndefined();
                this.setHeight("650px");
                this.setSelectable(true);
                this.longest = 1;

                this.addListener(new ItemListClickListener());
        }

        public void setLists(CharacterList charList, DkpList dkpList, RaidList raidList) {
                this.charLiist = charList;
                this.dkpList = dkpList;
                this.raidList = raidList;
        }

        private void itemListAddRow(Item addItem, final Items item) throws ConversionException, ReadOnlyException {
                Label itemname = new Label(item.getName(), Label.CONTENT_TEXT);
                itemname.addStyleName(item.getQuality().toLowerCase());
                addItem.getItemProperty("Name").setValue(itemname);
                addItem.getItemProperty("Price Normal").setValue(item.getPrice());
                addItem.getItemProperty("Price Heroic").setValue(item.getPrice_hc());
                addItem.getItemProperty("Slot").setValue(item.getSlot());
                addItem.getItemProperty("Type").setValue(item.getType().toString());
                this.requestRepaint();
        }

        private void itemListColumnHeaders() {
                ic.addContainerProperty("Name", Label.class, "");
                ic.addContainerProperty("Price Normal", Double.class, 0);
                ic.addContainerProperty("Price Heroic", Double.class, 0);
                ic.addContainerProperty("Slot", String.class, "");
                ic.addContainerProperty("Type", String.class, "");
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        public void printList() {
                clear();
                itemListColumnHeaders();
                if (this.getWidth() < 550) {
                        this.setWidth("550px");
                }
                List<Items> itemses = ItemDB.getItems();
                Collections.sort(itemses, new Comparator<Items>() {

                        @Override
                        public int compare(Items t, Items t1) {
                                return t.getName().compareToIgnoreCase(t1.getName());
                        }
                });

                for (final Items item : itemses) {
                        Item addItem = addItem(item);
                        itemListAddRow(addItem, item);
                        if (longest < item.getName().length() + 1) {
                                longest = item.getName().length() + 1;
                        }
                        this.setColumnWidth("Name", longest * 6);
                        this.requestRepaint();
                }

                this.setColumnCollapsingAllowed(true);
                this.setColumnCollapsed("Slot", true);
                this.setColumnCollapsed("Type", true);
        }

        private String filterString(Object value) {
                if (value == null) {
                        return "";
                } else {
                        return value.toString();
                }
        }

        public void filterSlot(Object value) {
                ic.removeContainerFilters("Slot");
                ic.addContainerFilter("Slot", filterString(value), true, false);
                this.requestRepaint();
        }

        public void filterType(Object value) {
                ic.removeContainerFilters("Type");
                ic.addContainerFilter("Type", filterString(value), true, false);
                this.requestRepaint();
        }

        public void filterName(Object value) {
                ic.removeContainerFilters("Name");
                ic.addContainerFilter("Name", filterString(value), true, false);
                this.requestRepaint();
        }

        @Override
        public void onItemInfoChange() {
                ItemDB.clearCache();
                update();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        private class ItemListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        Items item = (Items) event.getItemId();
                        PopUpControl pop = new PopUpControl(ItemList.this.getApplication());
                        pop.setItemList(itemList);
                        pop.setDkpList(dkpList);
                        pop.setCharacterList(charLiist);
                        pop.setRaidList(raidList);
                        pop.showProperItemWindow(item);
                }
        }
}
