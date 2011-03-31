/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.database.RaidDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.util.Collection;

/**
 *
 * @author alde
 */
public class RaidRewardList extends Table implements RaidRewardListener {

        IndexedContainer ic;
        private final RaidRewardList raidRewardList = this;
        private Raid raid;
        private final DkpList dkplist;
        private final CharacterList clist;

        public RaidRewardList(Raid raid, DkpList dkplist, CharacterList clist) {
                this.ic = new IndexedContainer();
                this.dkplist = dkplist;
                this.clist = clist;
                this.raid = raid;
                this.setSelectable(true);
                this.setSizeUndefined();
                this.setHeight("500px");
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
                RaidDB.clearCache();
                RaidDB.clearCache();
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

        private void printList() {
                Collection<RaidReward> rewards;
                rewards = RaidDB.getRewardsForRaid(this.raid.getId());
                for (RaidReward rreward : rewards) {
                        Item addItem = addItem(rreward);
                        raidListAddRow(addItem, rreward);
                }
        }

        private void raidListAddRow(Item addItem, RaidReward rreward) {
                addItem.getItemProperty("Comment").setValue(rreward.getComment());
                addItem.getItemProperty("Shares").setValue(rreward.getShares());
        }

        private class RewardListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        RaidReward rreward = (RaidReward) event.getItemId();
                        PopUpControl pop = new PopUpControl(RaidRewardList.this.getApplication());
                        pop.setRaidRewardList(raidRewardList);
                        pop.setCharacterList(clist);
                        pop.setDkpList(dkplist);
                        pop.showProperRaidRewardWindow(rreward);
                }
        }
}
