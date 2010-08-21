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
                if (isAdmin()) {
                        showCharEditWindow(user);
                } else {
                        showCharInfoWindow(user);
                }
        }

        private void showCharEditWindow(User user) throws IllegalArgumentException, NullPointerException {
                CharacterEditWindow info = new CharacterEditWindow(user);
                info.printInfo();
                info.addCharacterInfoListener(charList);
                info.addCharacterInfoListener(dkpList);
                app.getMainWindow().addWindow(info);
        }

        private void showCharInfoWindow(User user) throws NullPointerException, IllegalArgumentException {
                CharacterInfoWindow info = new CharacterInfoWindow(user);
                try {
                        info.printInfo();
                } catch (SQLException ex) {
                        Logger.getLogger(PopUpControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                app.getMainWindow().addWindow(info);
        }

        private boolean isAdmin() {
                final SiteUser siteUser = (SiteUser) app.getUser();
                return siteUser != null && siteUser.getLevel() == 1;
        }

        public void showProperItemWindow(Items item) throws NullPointerException, IllegalArgumentException {
                if (isAdmin()) {
                        showItemEditWindow(item);
                } else {
                        showItemInfoWindow(item);
                }
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
                app.getMainWindow().addWindow(info);
        }

        public void showProperRaidWindow(Raid raid) {
                if (isAdmin()) {
                        showRaidEditWindow(raid);
                } else {
                        showRaidInfoWindow(raid);
                }
        }

        private void showRaidInfoWindow(Raid raid) throws IllegalArgumentException, NullPointerException {
                RaidInfoWindow info = new RaidInfoWindow(raid);
                info.printInfo();
                app.getMainWindow().addWindow(info);
        }

        private void showRaidEditWindow(Raid raid) throws NullPointerException, IllegalArgumentException {
                RaidEditWindow info = new RaidEditWindow(raid);
                info.printInfo();
                info.addRaidInfoListener(raidList);
                app.getMainWindow().addWindow(info);
        }
}
