package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.XmlParser;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.ItemList;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Label;
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
        private final ItemList itemList;
        private int longest;

        public RaidLootList(Raid raid, DkpList dkplist, CharacterList clist, ItemList itemList) {
                this.ic = new IndexedContainer();
                this.raid = raid;
                this.dkplist = dkplist;
                this.clist = clist;
                this.itemList = itemList;
                this.setSelectable(true);
                this.setSizeUndefined();
                this.setHeight("500px");
                this.addListener(new RewardListClickListener());
                this.longest = 1;
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
                ic.addContainerProperty("Name", Label.class, "");
                ic.addContainerProperty("Item", Label.class, "");
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
                        for (RaidItem item : temp) {
                                Item addItem = ic.addItem(item);
                                raidListAddRow(addItem, item);
                                if (longest < item.getName().length() + 1) {
                                        longest = item.getName().length() + 1;
                                }
                        }
                        this.setColumnWidth("Item", longest * 6);
                } catch (SQLException ex) {
                        Logger.getLogger(RaidLootList.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private void raidListAddRow(Item addItem, RaidItem item) {
                Label looter = new Label(item.getLooter());
                CharacterDAO charDao = new CharacterDB();
                looter.addStyleName(charDao.getRoleForCharacter(item.getLooter()).toLowerCase().replace(" ", ""));
                addItem.getItemProperty("Name").setValue(looter);
                XmlParser xml = new XmlParser(item.getName());
                String quality = xml.parseXmlQuality().toLowerCase();
                Label itemname = new Label(item.getName());
                itemname.addStyleName(quality);
                addItem.getItemProperty("Item").setValue(itemname);
                addItem.getItemProperty("Price").setValue(item.getPrice());
                addItem.getItemProperty("Heroic").setValue(item.isHeroic());
                super.requestRepaint();
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
                                pop.setItemList(itemList);
                                pop.showProperRaidLootWindow(raid, ritem);

                        }
                }
        }
}
