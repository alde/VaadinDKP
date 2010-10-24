/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.raids.RaidList;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author bobo
 */
public class ItemList extends Table implements ItemInfoListener {

        private ItemDAO itemDAO;
        IndexedContainer ic;
        private final ItemList itemList = this;
        private CharacterList charLiist;
        private RaidList raidList;
        private DkpList dkpList;

        public ItemList(ItemDAO itemDAO) {
                this.itemDAO = itemDAO;
                this.ic = new IndexedContainer();
                this.setWidth("600px");
                this.setHeight("500px");
                this.setSelectable(true);

                this.addListener(new ItemListClickListener());
        }

        public void setLists(CharacterList charList, DkpList dkpList, RaidList raidList) {
                this.charLiist = charList;
                this.dkpList = dkpList;
                this.raidList = raidList;
        }

        private void itemListAddRow(Item addItem, final Items item) throws ConversionException, ReadOnlyException {
                addItem.getItemProperty("Name").setValue(item.getName());
                addItem.getItemProperty("Price Normal").setValue(item.getPrice());
                addItem.getItemProperty("Price Heroic").setValue(item.getPrice_hc());
                addItem.getItemProperty("Slot").setValue(item.getSlot());
                addItem.getItemProperty("Type").setValue(item.getType().toString());
        }

        private void itemListColumnHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Name", String.class, "");
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
                List<Items> itemses = itemDAO.getItems();

                for (final Items item : itemses) {
                        Item addItem = addItem(item);
                        itemListAddRow(addItem, item);
                }
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
        }

        public void filterType(Object value) {
                ic.removeContainerFilters("Type");
                ic.addContainerFilter("Type", filterString(value), true, false);
        }

        public void filterName(Object value) {
                ic.removeContainerFilters("Name");
                ic.addContainerFilter("Name", filterString(value), true, false);
        }

        @Override
        public void onItemInfoChange() {
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
                        if (event.isDoubleClick()) {
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
}
