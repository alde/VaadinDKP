/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.items.windows.ItemInfoWindow;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidItem;
import com.unknown.entity.raids.RaidReward;
import com.unknown.entity.raids.RaidRewardList;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

/**
 *
 * @author alde
 */
public class RaidInfoWindow extends Window {

        private final Raid raid;

        public RaidInfoWindow(Raid raid) {
                this.raid = raid;
                this.setPositionX(600);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.setCaption(raid.getName());

        }

        public void printInfo() {
                raidInformation();

                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);

                RaidRewardList rrList = new RaidRewardList(raid);
                hzl.addComponent(rrList);
                hzl.addComponent(getTable(lootList(raid)));

                addComponent(hzl);
        }

        private void raidInfoWindowLootListAddRow(Item addItem, RaidItem item) throws ReadOnlyException, ConversionException {
                addItem.getItemProperty("Name").setValue(item.getLooter());
                addItem.getItemProperty("Item").setValue(item.getName());
                addItem.getItemProperty("Price").setValue(item.getPrice());
                addItem.getItemProperty("Heroic").setValue(item.isHeroic());
        }

        private void raidInfoWindowLootListSetHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", String.class, "");
                tbl.addContainerProperty("Item", String.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.addContainerProperty("Heroic", String.class, "");
        }

        private void raidInformation() {
                addComponent(new Label("Raid information"));
                addComponent(new Label("Zone: " + raid.getName()));
                addComponent(new Label("Comment: " + raid.getComment()));
                addComponent(new Label("Date: " + raid.getDate()));
        }

        private Table lootList(Raid raid) {
                Table tbl = new Table();
                tbl.addStyleName("small");
                raidInfoWindowLootListSetHeaders(tbl);
                tbl.setHeight(150);
                for (RaidItem item : raid.getRaidItems()) {
                        Item addItem = tbl.addItem(item);
                        raidInfoWindowLootListAddRow(addItem, item);
                }
                tbl.addListener(new LootListClickListener());
                return tbl;
        }

        private Component getTable(Table rewards) {
                if (rewards.size() > 0) {
                        return rewards;
                } else {
                        return new Label("No rewards in this raid.");
                }
        }
        
        private class LootListClickListener implements ItemClickListener {

                public LootListClickListener() {
                }

                @Override
                public void itemClick(ItemClickEvent event) {
                        ItemDAO itemDao = new ItemDB();
                        RaidItem raiditem = (RaidItem) event.getItemId();
                        Items item = itemDao.getSingleItem(raiditem.getName());
                        ItemInfoWindow info = new ItemInfoWindow(item);
                        info.printInfo();
                        getApplication().getMainWindow().addWindow(info);
                }
        }
}
