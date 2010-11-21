/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items;

import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author alde
 */
public class MultiplierList extends Table {

        ItemDAO itemDao = null;
        IndexedContainer ic;
        List<Multiplier> multipliers = new ArrayList<Multiplier>();

        public MultiplierList() {
                this.ic = new IndexedContainer();
                this.setSelectable(true);
                this.setEditable(true);
                this.setSizeUndefined();
                this.itemDao = new ItemDB();
                setContainerDataSource(ic);
                printList();
        }

        public void update() {
                ic.removeAllItems();
                printList();
        }

        public void clear() {
                ic.removeAllItems();
                multipliers.clear();
        }

        public void printList() {
                clear();
                multipliers.addAll(itemDao.getMultipliers());
                addContainerProperty("Itemlevel", Integer.class, "");
                addContainerProperty("Multiplier", Double.class, 0);
                addContainerProperty("Delete", CheckBox.class, false);
                setSortContainerPropertyId("Itemlevel");
                for (Multiplier mp : multipliers) {
                        Item addItem = ic.addItem(mp);
                        priceTableSetRow(addItem, mp);
                }
        }

        private void priceTableSetRow(Item addItem, Multiplier mp) {
                addItem.getItemProperty("Itemlevel").setValue(mp.getIlvl());
                addItem.getItemProperty("Multiplier").setValue(mp.getMultiplier());
                addItem.getItemProperty("Delete").setValue("");
        }

        public void updateIlvls() {
                List<Integer> idstodelete = new ArrayList<Integer>();
                for (Iterator i = ic.getItemIds().iterator(); i.hasNext();) {
                        Multiplier iid = (Multiplier) i.next();
                        Item item = ic.getItem(iid);
                        if (Boolean.parseBoolean(item.getItemProperty("Delete").toString())) {
                                idstodelete.add(iid.getId());
                        } else {
                                itemDao.updateItemLevels(iid.getId(), Integer.parseInt(item.getItemProperty("Itemlevel").toString()), Double.parseDouble((item.getItemProperty("Multiplier")).toString()));
                        }
                }
                for (int i : idstodelete) {
                        itemDao.deleteItemLevelsMultiplier(i);
                }
                update();
        }
}
