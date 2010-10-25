/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.*;
import com.unknown.entity.items.ItemList;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author alde
 */
public class RaidList extends Table implements RaidInfoListener {

        private RaidDAO raidDAO;
        IndexedContainer ic;
        private final RaidList raidList = this;
        private CharacterList clist;
        private DkpList dkplist;
        private ItemList itemList;

        public RaidList(RaidDAO raidDAO) {
                this.raidDAO = raidDAO;
                this.ic = new IndexedContainer();
                this.setSelectable(true);
                this.setSizeUndefined();
                this.setHeight("500px");
                this.addListener(new RaidListClickListener());
                raidListSetHeaders();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        public void setCharList(CharacterList clist) {
                this.clist = clist;
        }

        public void setDkpList(DkpList dkplist) {
                this.dkplist = dkplist;
        }

        public void setItemList(ItemList itemList) {
                this.itemList = itemList;
        }

        @Override
        public void onRaidInfoChanged() {
                update();
        }

        private void raidListAddRow(Item addItem, final Raid raid) throws ReadOnlyException, ConversionException {
                addItem.getItemProperty("Zone").setValue(raid.getName());
                addItem.getItemProperty("Comment").setValue(raid.getComment());
                addItem.getItemProperty("Date").setValue(raid.getDate());
        }

        private void raidListSetHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Zone", String.class, "");
                ic.addContainerProperty("Comment", String.class, "");
                ic.addContainerProperty("Date", String.class, "");
                this.setContainerDataSource(ic);
        }

        public void printList() {
                List<Raid> raids = raidDAO.getRaids();

                for (final Raid raid : raids) {
                        Item addItem = addItem(raid);
                        raidListAddRow(addItem, raid);
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

        private class RaidListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isDoubleClick()) {
                                Raid raid = (Raid) event.getItemId();
                                PopUpControl pop = new PopUpControl(RaidList.this.getApplication());
                                pop.setRaidList(raidList);
                                pop.setCharacterList(clist);
                                pop.setDkpList(dkplist);
                                pop.setItemList(itemList);
                                pop.showProperRaidWindow(raid);
                        }
                }
        }
}
