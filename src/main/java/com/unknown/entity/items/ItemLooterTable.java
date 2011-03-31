/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.database.CharDB;
import com.unknown.entity.database.RaidDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author alde
 */
public class ItemLooterTable extends Table {

        IndexedContainer ic = new IndexedContainer();
        Items item;

        public ItemLooterTable(Items item) {
                this.item = item;
                printList();
        }

        private void printList() {
                this.ic = new IndexedContainer();
                itemLooterSetHeaders();
                setHeight("250px");
                setContainerDataSource(ic);
                List<ItemLooter> looterlist = item.getLooterList();
                for (ItemLooter looter : looterlist) {
                        Item addItem = addItem(looter);
                        itemLooterAddRow(addItem, looter);

                }
        }

        public void update() {
                this.removeAllItems();
                this.requestRepaintRequests();
                printList();
                this.requestRepaint();
        }

        private void itemLooterAddRow(Item addItem, ItemLooter looter) throws ConversionException, ReadOnlyException {
                boolean isheroic = RaidDB.getLootedHeroic(looter.getName(), item.getId(), looter.getPrice());
                Label looterLabel = new Label(looter.getName());
                looterLabel.addStyleName(CharDB.getRoleForCharacter(looter.getName()).toLowerCase().replace(" ", ""));
                addItem.getItemProperty("Name").setValue(looterLabel);
                addItem.getItemProperty("Price").setValue(looter.getPrice());
                addItem.getItemProperty("Heroic").setValue(isheroic);
                addItem.getItemProperty("Raid").setValue(looter.getRaid());
                addItem.getItemProperty("Date").setValue(looter.getDate());
        }

        private void itemLooterSetHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Name", Label.class, "");
                ic.addContainerProperty("Price", Double.class, 0);
                ic.addContainerProperty("Heroic", Boolean.class, false);
                ic.addContainerProperty("Raid", String.class, "");
                ic.addContainerProperty("Date", String.class, "");
        }
}
