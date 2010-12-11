/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.character.windows.AddNewUserWindow;
import com.unknown.entity.items.windows.EditDefaultPricesWindow;
import com.unknown.entity.LoginWindow;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.character.windows.CharacterAddWindow;
import com.unknown.entity.character.SiteUser;
import com.unknown.entity.items.ItemInfoListener;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.items.windows.EditMultiplierWindow;
import com.unknown.entity.items.windows.ItemAddWindow;
import com.unknown.entity.raids.RaidInfoListener;
import com.unknown.entity.raids.RaidList;
import com.unknown.entity.raids.windows.RaidAddWindow;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class AdminPanel extends HorizontalLayout implements MyLoginListener {

        private final Button loginBtn = new Button();
        private final Button addCharacterBtn = new Button("Add Character");
        private final Button addRaidBtn = new Button("Add Raid");
        private final Button addItmBtn = new Button("Add Item");
        private final Button editDefaultBtn = new Button("Edit Default prices");
        private final Button editMultipliersBtn = new Button("Edit Multipliers");
        private final Button addUserBtn = new Button("Add User");
        private final Button editUserBtn = new Button("Edit User");
        private final Button editZoneBtn = new Button("Edit Zones");
        private final Button logOutButton = new Button("");
        private final Button refreshBtn = new Button();
        private List<CharacterInfoListener> listeners = new ArrayList<CharacterInfoListener>();
        private List<RaidInfoListener> raidlisteners = new ArrayList<RaidInfoListener>();
        private List<ItemInfoListener> itemlisteners = new ArrayList<ItemInfoListener>();
        RaidList raidList = null;
        CharacterList characterList = null;
        DkpList dkpList = null;
        ItemList itemList = null;

        public AdminPanel() {
                setListeners();
                styleLoginLogoutRefresh();
                this.setSpacing(true);
        }

        private void notifyListeners() {
                for (CharacterInfoListener characterListener : listeners) {
                        characterListener.onCharacterInfoChange();
                }
                for (RaidInfoListener raidListener : raidlisteners) {
                        raidListener.onRaidInfoChanged();
                }
                for (ItemInfoListener itemInfoListener : itemlisteners) {
                        itemInfoListener.onItemInfoChange();
                }
        }

        public void addCharacterInfoListener(CharacterInfoListener listener) {
                listeners.add(listener);
        }

        public void addRaidInfoListener(RaidInfoListener raidlistener) {
                raidlisteners.add(raidlistener);
        }

        public void addItemInfoListener(ItemInfoListener itemlistener) {
                itemlisteners.add(itemlistener);
        }

        private void styleLoginLogoutRefresh() {
                loginBtn.setIcon(new ThemeResource("../shared/key.png"));
                loginBtn.setStyle(Button.STYLE_LINK);
                logOutButton.setStyle(Button.STYLE_LINK);
                logOutButton.setIcon(new ThemeResource("../shared/key.png"));
                refreshBtn.setIcon(new ThemeResource("../shared/refresh.png"));
                refreshBtn.setStyle(Button.STYLE_LINK);
        }

        private void setListeners() {
                loginBtn.addListener(new LoginClickListener());
                addCharacterBtn.addListener(new AddCharacterListener());
                addRaidBtn.addListener(new AddRaidListener());
                addItmBtn.addListener(new AddItemListener());
                editDefaultBtn.addListener(new EditDefaultsListener());
                editMultipliersBtn.addListener(new EditMultipliersListener());
                addUserBtn.addListener(new AddUserListener());
                editUserBtn.addListener(new EditUserListener());
                editZoneBtn.addListener(new EditZonesListener());
                logOutButton.addListener(new LogOutListener());
                refreshBtn.addListener(new refreshListener());
        }

        public void init() {
                if (!isAdmin() || !isSuperAdmin()) {
                        addComponent(loginBtn);
                        this.addComponent(refreshBtn);
                }
        }

        private void login() {
                this.removeAllComponents();
                if (isAdmin() || isSuperAdmin()) {
                        this.addComponent(logOutButton);
                        this.addComponent(addCharacterBtn);
                        this.addComponent(addItmBtn);
                        this.addComponent(addRaidBtn);
                        if (isSuperAdmin()) {
                                this.addComponent(editDefaultBtn);
                                this.addComponent(editMultipliersBtn);
                        }
                        this.addComponent(editZoneBtn);
                        if (isSuperAdmin()) {
                                this.addComponent(addUserBtn);
                        }
                        this.addComponent(editUserBtn);
                        this.addComponent(refreshBtn);
                } else {
                        this.addComponent(loginBtn);
                        this.addComponent(refreshBtn);
                }

        }

        private boolean isAdmin() {
                final SiteUser siteUser = (SiteUser) getApplication().getUser();
                return siteUser != null && siteUser.getLevel() == 1;
        }

        private boolean isSuperAdmin() {
                final SiteUser siteUser = (SiteUser) getApplication().getUser();
                return siteUser != null && siteUser.getLevel() == 2;
        }

        private Window getMainWindow() {
                return getApplication().getMainWindow();
        }

        @Override
        public void onLogin() {
                login();
        }

        public void setRaidList(RaidList raidList) {
                this.raidList = raidList;
        }

        public void setItemList(ItemList itemList) {
                this.itemList = itemList;
        }

        public void setDkpList(DkpList dkpList) {
                this.dkpList = dkpList;
        }

        public void setCharacterList(CharacterList characterList) {
                this.characterList = characterList;
        }

        private class AddItemListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        ItemAddWindow addItem = new ItemAddWindow();
                        addItem.printInfo();
                        addItem.addItemInfoListener(itemList);
                        getMainWindow().addWindow(addItem);
                }
        }

        private class AddRaidListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        RaidAddWindow addRaid = new RaidAddWindow();
                        addRaid.printInfo();
                        addRaid.addRaidInfoListener(raidList);
                        getMainWindow().addWindow(addRaid);
                }
        }

        private class AddCharacterListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        CharacterAddWindow addChar = new CharacterAddWindow();
                        addChar.printInfo();
                        addChar.addCharacterInfoListener(characterList);
                        addChar.addCharacterInfoListener(dkpList);
                        getMainWindow().addWindow(addChar);
                }
        }

        private class LoginClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        if (getMainWindow().getApplication().getUser() == null) {
                                LoginWindow loginWindow = new LoginWindow();
                                loginWindow.addLoginListener(AdminPanel.this);
                                getMainWindow().addWindow(loginWindow);
                                loginWindow.addCharacterInfoListener(characterList);
                                loginWindow.addCharacterInfoListener(dkpList);
                                loginWindow.attach();

                        } else {
                                getMainWindow().addComponent(new Label("User: " + (getApplication() != null ? getApplication().getUser() : "")));
                        }
                }
        }

        private class LogOutListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        if (getMainWindow().getApplication().getUser() != null) {
                                getMainWindow().getApplication().setUser(null);
                                notifyListeners();
                                login();
                        }
                }
        }

        private class EditDefaultsListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        try {
                                EditDefaultPricesWindow editDefaults = new EditDefaultPricesWindow();
                                editDefaults.printInfo();
                                getMainWindow().addWindow(editDefaults);
                        } catch (SQLException ex) {
                                Logger.getLogger(AdminPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }

        private class AddUserListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        AddNewUserWindow newUser = new AddNewUserWindow();
                        newUser.printInfo();
                        getMainWindow().addWindow(newUser);
                }
        }

        private class EditZonesListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {

                        EditZonesWindow editZones = new EditZonesWindow();
                        editZones.printInfo();
                        editZones.addRaidInfoListener(raidList);
                        getMainWindow().addWindow(editZones);
                }
        }

        private class EditMultipliersListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        EditMultiplierWindow editMP = new EditMultiplierWindow();
                        editMP.addItemInfoListener(itemList);
                        editMP.printInfo();
                        getMainWindow().addWindow(editMP);
                }
        }

        private class EditUserListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        EditUserWindow editUser = new EditUserWindow(getApplication());
                        editUser.printInfo();
                        getMainWindow().addWindow(editUser);
                }
        }

        private class refreshListener implements ClickListener {

                public refreshListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        notifyListeners();
                }
        }
}
