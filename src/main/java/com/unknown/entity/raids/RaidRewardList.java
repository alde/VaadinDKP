/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                RaidDAO raidDao = new RaidDB();
                Collection<RaidReward> rewards;
                try {
                        rewards = raidDao.getRewardsForRaid(this.raid.getId());
                        System.out.println("++++ ---- " + raid.getRaidRewards());
                        for (RaidReward rreward : rewards) {
                                Item addItem = addItem(rreward);
                                raidListAddRow(addItem, rreward);
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(RaidRewardList.class.getName()).log(Level.SEVERE, null, ex);
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
                        if (event.isDoubleClick()) {
                                RaidReward rreward = (RaidReward) event.getItemId();
                                PopUpControl pop = new PopUpControl(RaidRewardList.this.getApplication());
                                pop.setRaidRewardList(raidRewardList);
                                pop.setCharacterList(clist);
                                pop.setDkpList(dkplist);
                                pop.showProperRaidRewardWindow(rreward);
                        }
                }
        }
}
