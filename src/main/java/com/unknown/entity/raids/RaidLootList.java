package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidLootList extends Table implements RaidLootListener {

        IndexedContainer ic;
        private final RaidLootList raidLootList = this;
        private Raid raid;
        private final DkpList dkplist;
        private final CharacterList clist;

        public RaidLootList(Raid raid, DkpList dkplist, CharacterList clist) {
                this.ic = new IndexedContainer();
                this.raid = raid;
                this.dkplist = dkplist;
                this.clist = clist;
                this.setSelectable(true);
                this.setSizeUndefined();
                this.setHeight("500px");
//                this.setWidth("450px");
                this.addListener(new RewardListClickListener());
                raidRewardListSetHeaders();
                printList();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        @Override
        public void onRaidInfoChanged() {
                update();
        }

        private void raidRewardListSetHeaders() {
                ic.addContainerProperty("Name", String.class, "");
                ic.addContainerProperty("Item", String.class, "");
                ic.addContainerProperty("Price", Double.class, 0);
                ic.addContainerProperty("Heroic", String.class, "");
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        private void printList() {
                ItemDAO itemDao = new ItemDB();
                ArrayList<RaidItem> temp;
                try {
                        temp = itemDao.getItemsForRaid(raid.getId());
                        System.out.println("Raid Loots: " + temp.size());
                        for (RaidItem item : temp) {
                                System.out.println("Loot ... " + item.getName());
                                Item addItem = ic.addItem(item);
                                raidListAddRow(addItem, item);
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(RaidLootList.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private void raidListAddRow(Item addItem, RaidItem item) {
                addItem.getItemProperty("Name").setValue(item.getLooter());
                addItem.getItemProperty("Item").setValue(item.getName());
                addItem.getItemProperty("Price").setValue(item.getPrice());
                addItem.getItemProperty("Heroic").setValue(item.isHeroic());
        }

        private class RewardListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isDoubleClick()) {
                                RaidItem ritem = (RaidItem) event.getItemId();
                                PopUpControl pop = new PopUpControl(RaidLootList.this.getApplication());
                                pop.setRaidLootList(raidLootList);
                                pop.setCharacterList(clist);
                                pop.setDkpList(dkplist);
                                pop.showProperRaidLootWindow(raid, ritem);
                        }
                }
        }
}
