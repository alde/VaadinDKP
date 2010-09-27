/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.raids.windows.RewardAttendantsWindow;
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
public class RaidRewardList extends Table implements RaidInfoListener {

        IndexedContainer ic;
        private final RaidRewardList raidRewardList = this;
        private Raid raid;

        public RaidRewardList(Raid raid) {
                this.ic = new IndexedContainer();
                this.raid = raid;
                this.setSelectable(true);
                this.setHeight("500px");
                this.setWidth("300px");
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
                ic.addContainerProperty("Comment", String.class, "");
                ic.addContainerProperty("Shares", Integer.class, "");
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        public void printList() {
                List<RaidReward> rewards = raid.getRaidRewards();
                System.out.println("++++ ---- "+ raid.getRaidRewards());
                for (RaidReward rreward : rewards) {
                        Item addItem = addItem(rreward);
                        raidListAddRow(addItem, rreward);
                }
        }

        private void raidListAddRow(Item addItem, RaidReward rreward) {
                System.out.println(rreward.getComment());
                addItem.getItemProperty("Comment").setValue(rreward.getComment());
                addItem.getItemProperty("Shares").setValue(rreward.getShares());
        }

        private class RewardListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        RaidReward rreward = (RaidReward) event.getItemId();
                        RewardAttendantsWindow info = new RewardAttendantsWindow(rreward.getRewardChars());
                        info.printInfo();
                        getApplication().getMainWindow().addWindow(info);
                }
        }

}
