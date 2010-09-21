/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.dao.RaidDAO;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author alde
 */
public class RaidRewardList extends Table implements RaidInfoListener {

        // private RaidDAO raidDAO;
        IndexedContainer ic;
        private final RaidRewardList raidRewardList = this;
        private Raid raid;

        public RaidRewardList(Raid raid) {
              //  this.raidDAO = raidDAO;
                this.ic = new IndexedContainer();
                this.raid = raid;
                this.setSelectable(true);
                this.setHeight("500px");
                this.setWidth("300px");
                //       this.addListener(new RaidListClickListener());
                raidRewardListSetHeaders();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        @Override
        public void onRaidInfoChanged() {
                throw new UnsupportedOperationException("Not supported yet.");
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
                for (RaidReward rreward : rewards) {
                        Item addItem = addItem(rreward);
                        raidListAddRow(addItem, rreward);
                }
        }

        private void raidListAddRow(Item addItem, RaidReward rreward) {
                addItem.getItemProperty("Comment").setValue(rreward.getComment());
                addItem.getItemProperty("Shares").setValue(rreward.getShares());
        }
}
