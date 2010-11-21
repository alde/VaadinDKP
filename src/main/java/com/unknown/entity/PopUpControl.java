/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import com.unknown.entity.character.*;
import com.unknown.entity.character.windows.*;
import com.unknown.entity.items.*;
import com.unknown.entity.items.windows.*;
import com.unknown.entity.raids.*;
import com.unknown.entity.raids.windows.*;
import com.vaadin.Application;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class PopUpControl extends Window {

        private ItemList itemList;
        private RaidList raidList;
        private CharacterList charList;
        private DkpList dkpList;
        private final Application app;
        private RaidRewardList raidRewardList;
        private RaidLootList raidLootList;

        public PopUpControl(Application app) {
                this.app = app;
        }

        public void setItemList(ItemList itemList) {
                this.itemList = itemList;
        }

        public void setRaidList(RaidList raidList) {
                this.raidList = raidList;
        }

        public void setCharacterList(CharacterList charList) {
                this.charList = charList;
        }

        public void setDkpList(DkpList dkpList) {
                this.dkpList = dkpList;
        }

        public void showProperCharWindow(User user) throws IllegalArgumentException, NullPointerException {
//                long start = System.currentTimeMillis();

                if (isAdmin()) {
                        showCharEditWindow(user);
                } else {
                        showCharInfoWindow(user);
                }
//                long elapsed = System.currentTimeMillis() - start;
                // System.out.println("Time to open character window: " + elapsed);
        }

        private void showCharEditWindow(User user) throws IllegalArgumentException, NullPointerException {
                CharacterEditWindow info = new CharacterEditWindow(user, app, raidList, itemList);
                try {
                        info.printInfo();
                        info.addCharacterInfoListener(charList);
                        info.addCharacterInfoListener(dkpList);
                        app.getMainWindow().addWindow(info);
                } catch (SQLException ex) {
                        Logger.getLogger(PopUpControl.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private void showCharInfoWindow(User user) throws NullPointerException, IllegalArgumentException {
                CharacterInfoWindow info = new CharacterInfoWindow(user, app, raidList, itemList);
                try {
                        info.printInfo();
                        app.getMainWindow().addWindow(info);
                } catch (SQLException ex) {
                        Logger.getLogger(PopUpControl.class.getName()).log(Level.SEVERE, null, ex);
                }

        }

        private boolean isAdmin() {
                final SiteUser siteUser = (SiteUser) app.getUser();
                return siteUser != null && siteUser.getLevel() == 1;
        }
        private boolean isSuperAdmin() {
                final SiteUser siteUser = (SiteUser) app.getUser();
                return siteUser != null && siteUser.getLevel() == 2;
        }

        public void showProperItemWindow(Items item) throws NullPointerException, IllegalArgumentException {
//                long start = System.currentTimeMillis();
                if (isAdmin()) {
                        showItemEditWindow(item);
                } else {
                        showItemInfoWindow(item);
                }
//                long elapsed = System.currentTimeMillis() - start;
                // System.out.println("Time to open item window: " + elapsed);
        }

        private void showItemInfoWindow(Items item) throws NullPointerException, IllegalArgumentException {
                ItemInfoWindow info = new ItemInfoWindow(item);
                info.printInfo();
                app.getMainWindow().addWindow(info);
        }

        private void showItemEditWindow(Items item) throws NullPointerException, IllegalArgumentException {
                ItemEditWindow info = new ItemEditWindow(item);
                info.printInfo();
                info.addItemInfoListener(itemList);
                info.addCharacterInfoListener(charList);
                info.addCharacterInfoListener(dkpList);
                app.getMainWindow().addWindow(info);
        }

        public void showProperRaidWindow(Raid raid) {
//                long start = System.currentTimeMillis();
                if (isAdmin()) {
                        showRaidEditWindow(raid);
                } else {
                        showRaidInfoWindow(raid);
                }
//                long elapsed = System.currentTimeMillis() - start;
                // System.out.println("Time to open raid window: " + elapsed);
        }

        private void showRaidInfoWindow(Raid raid) throws IllegalArgumentException, NullPointerException {
                RaidInfoWindow info = new RaidInfoWindow(raid, dkpList, charList, itemList);
                info.printInfo();
                app.getMainWindow().addWindow(info);
        }

        private void showRaidEditWindow(Raid raid) throws NullPointerException, IllegalArgumentException {
                RaidEditWindow info = new RaidEditWindow(raid, dkpList, charList, itemList);
                info.printInfo();
                info.addRaidInfoListener(raidList);
                app.getMainWindow().addWindow(info);
                // System.out.println(itemList.toString());
        }

        public void setRaidRewardList(RaidRewardList raidRewardList) {
                this.raidRewardList = raidRewardList;
        }

        public void showProperRaidRewardWindow(RaidReward rreward) {
//                long start = System.currentTimeMillis();
                if (isAdmin()) {
                        showRaidRewardEditWindow(rreward);
                } else {
                        showRaidRewardInfoWindow(rreward);
                }
//                long elapsed = System.currentTimeMillis() - start;
                // System.out.println("Time to open raid reward window: " + elapsed);
        }

        private void showRaidRewardInfoWindow(RaidReward rreward) {
                RaidRewardAttendantsWindow info = new RaidRewardAttendantsWindow(rreward.getRewardChars());
                info.printInfo();
                info.addRaidRewardInfoListener(raidRewardList);
                app.getMainWindow().addWindow(info);
        }

        private void showRaidRewardEditWindow(RaidReward rreward) {
                RaidRewardEditWindow info = new RaidRewardEditWindow(rreward);
                info.printInfo();
                info.addRaidRewardInfoListener(raidRewardList);
                info.addCharacterInfoListener(dkpList);
                info.addCharacterInfoListener(charList);
                app.getMainWindow().addWindow(info);
        }

        public void setRaidLootList(RaidLootList raidLootList) {
                this.raidLootList = raidLootList;
        }

        public void showProperRaidLootWindow(Raid raid, RaidItem ritem) {
//                long start = System.currentTimeMillis();
                if (isAdmin()) {
                        showRaidLootEditWindow(raid, ritem);
                } else {
                        showItemInfoWindow(ritem.toItem());
                }
//                long elapsed = System.currentTimeMillis()-start;
// System.out.println("Time to open raid loot window: " + elapsed);
        }

        private void showRaidLootEditWindow(Raid raid, RaidItem ritem) {
                RaidLootEditWindow info = new RaidLootEditWindow(raid, ritem);
                info.printInfo();
                info.addRaidLootInfoListener(raidLootList);
                info.addCharacterInfoListener(dkpList);
                info.addCharacterInfoListener(charList);
                info.addItemInfoListener(itemList);
                app.getMainWindow().addWindow(info);
        }
}
