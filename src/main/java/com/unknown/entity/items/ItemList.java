/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.XmlParser;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.raids.RaidList;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
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
        private int longest;

        public ItemList(ItemDAO itemDAO) {
                this.itemDAO = itemDAO;
                this.ic = new IndexedContainer();
                this.setSizeUndefined();
                this.setHeight("500px");
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
//                XmlParser xml = new XmlParser(item.getName());
//                String quality = xml.parseXmlQuality().toLowerCase();
                Label itemname = new Label(item.getName(), Label.CONTENT_TEXT);
                itemname.addStyleName(item.getQuality().toLowerCase());
                addItem.getItemProperty("Name").setValue(itemname);
                addItem.getItemProperty("Price Normal").setValue(item.getPrice());
                addItem.getItemProperty("Price Heroic").setValue(item.getPrice_hc());
                addItem.getItemProperty("Slot").setValue(item.getSlot());
                addItem.getItemProperty("Type").setValue(item.getType().toString());
                this.requestRepaint();
        }

        private void itemListColumnHeaders() throws UnsupportedOperationException {
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
                List<Items> itemses = itemDAO.getItems();

                for (final Items item : itemses) {
                        Item addItem = addItem(item);
                        itemListAddRow(addItem, item);
                          if (longest < item.getName().length() + 1) {
                                longest = item.getName().length() + 1;
                        }
                        this.setColumnWidth("Name", longest * 6);
                        this.requestRepaint();
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
