/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.items.windows.ItemEditWindow;
import com.unknown.entity.items.windows.ItemInfoWindow;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.character.SiteUser;
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
public class ItemList extends Table {

        private ItemDAO itemDAO;

        IndexedContainer ic;

        public ItemList(ItemDAO itemDAO) {
                this.itemDAO = itemDAO;
                this.ic = new IndexedContainer();
                this.setHeight("500px");

                this.addListener(new ItemListClickListener());
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
        public void filter(Object value, String column) {
                ic.removeAllContainerFilters();
                ic.addContainerFilter(column, filterString(value), true, false);
        }

        private String filterString(Object value) {
                if (value == null) {
                        return "";
                } else {
                        return value.toString();
                }
        }

        private boolean isAdmin() {
                final SiteUser siteUser = (SiteUser) getApplication().getUser();
                return siteUser != null && siteUser.getLevel() == 1;
        }

        private class ItemListClickListener implements ItemClickListener {

                public ItemListClickListener() {
                }

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isCtrlKey()) {
                                Items item = (Items) event.getItemId();
                                PopUpControl pop = new PopUpControl(ItemList.this.getApplication());
                                pop.showProperItemWindow(item);
                        }
                }
        }
}
