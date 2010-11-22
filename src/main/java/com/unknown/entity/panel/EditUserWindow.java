/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.character.SiteUser;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.ILoginDao;
import com.unknown.entity.dao.LoginDao;
import com.unknown.entity.database.CharacterDB;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
class EditUserWindow extends Window {

        CharacterDAO characterDao;
        final ComboBox allUsernames;
        final TextField oldPassword;
        final TextField newPassword;
        final TextField newPasswordCheck;
        final ComboBox level;
        final String user;
        private ILoginDao loginDao;
        Application app;
        VerticalLayout vert;
        Label error;

        EditUserWindow(Application app) {
                this.characterDao = new CharacterDB();
                this.vert = new VerticalLayout();
                this.loginDao = new LoginDao();
                this.app = app;
                SiteUser sUser = (SiteUser) app.getUser();
                this.user = sUser.getName();
                this.allUsernames = new ComboBox("Username: ");
                if (isSuperAdmin()) {
                        this.oldPassword = new TextField(user + "'s Password: ");

                } else {
                        this.oldPassword = new TextField("Old Password: ");
                }
                this.newPassword = new TextField("New Password: ");
                this.newPasswordCheck = new TextField("Confirm New Password: ");
                this.level = new ComboBox("User Level: ");
                this.level.addItem("Admin");
                this.level.addItem("SuperAdmin");
                this.level.setNullSelectionAllowed(false);
                this.level.setImmediate(true);
                this.level.setValue(getUserLevel(user));
                this.oldPassword.setSecret(true);
                this.newPassword.setSecret(true);
                this.newPasswordCheck.setSecret(true);
                this.oldPassword.setImmediate(true);
                this.newPassword.setImmediate(true);
                this.newPasswordCheck.setImmediate(true);
                this.setCaption("Edit User");
                this.center();
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
                this.error = new Label();
                doFillAllUsernames();
        }

        void printInfo() {
                error.setValue("");
                if (isAdmin()) {
                        vert.addComponent(new Label(user));
                } else if (isSuperAdmin()) {
                        vert.addComponent(allUsernames);
                }
                vert.addComponent(oldPassword);
                vert.addComponent(newPassword);
                vert.addComponent(newPasswordCheck);
                if (isSuperAdmin()) {
                        vert.addComponent(level);
                }
                Button updateBtn = new Button("Update User");
                updateBtn.addListener(new UpdateBtnListener());
                Button closeBtn = new Button("Close");
                closeBtn.addListener(new CloseBtnListener());

                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(updateBtn);
                hzl.addComponent(closeBtn);
                vert.addComponent(hzl);
                vert.setSpacing(true);
                vert.addComponent(error);
                this.addComponent(vert);
        }

        private void updateUser() {
                if (verifyOldPassword()) {
                        if (checkNewPasswordMatches()) {
                                int userlevel = 1;
                                if (level.getValue().toString().equals("Admin")) {
                                        userlevel = 1;
                                } else if (level.getValue().toString().equals("SuperAdmin")) {
                                        userlevel = 2;
                                }
                                String pass = hashPassword(newPassword.getValue().toString());
                                if (isAdmin()) {
                                        characterDao.updateSiteUser(user, pass, userlevel);
                                        error.setValue("Password Changed.");
                                } else if (isSuperAdmin()) {
                                        characterDao.updateSiteUser(allUsernames.getValue().toString(), pass, userlevel);
                                        error.setValue("Password Changed.");
                                }
                        } else {
                                error.setValue("Passwords don't match!");
                        }
                } else {
                        if (isAdmin()) {
                                error.setValue("Old password is wrong.");
                        } else if (isSuperAdmin()) {
                                error.setValue(user + "'s password is wrong.");
                        }
                }
                if (!error.getValue().toString().isEmpty()) {
                        error.setStyleName("error");
                } else {
                        error.removeStyleName("error");
                }
        }

        private boolean verifyOldPassword() {
                SiteUser siteUser = loginDao.checkLogin(user, hashPassword(oldPassword.getValue().toString()));
                if (siteUser != null) {
                        return true;
                } else {
                        return false;
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

        private void doFillAllUsernames() {
                List<String> names = new ArrayList<String>();
                names.addAll(characterDao.getSiteUsers());
                for (String s : names) {
                        allUsernames.addItem(s);
                }
                allUsernames.setValue(user);
                allUsernames.setImmediate(true);
                allUsernames.addListener(new UserNameChangeListener());
        }

        public String hashPassword(String password) {
                String hashword = null;
                try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        md5.update(password.getBytes());
                        BigInteger hash = new BigInteger(1, md5.digest());
                        hashword = hash.toString(16);
                        if (hashword.length() == 31) {
                                hashword = "0" + hashword;
                        }
                } catch (NoSuchAlgorithmException nsae) {
                        nsae.printStackTrace();
                }
                return hashword;
        }

        private boolean checkNewPasswordMatches() {
                return newPassword.getValue().equals(newPasswordCheck.getValue());
        }

        private String getUserLevel(String name) {
                int userLevel = characterDao.getSiteUserLevel(name);
                if (userLevel == 2) {
                        return "SuperAdmin";
                } else {
                        return "Admin";
                }
        }

        private class CloseBtnListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class UpdateBtnListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        updateUser();
                }
        }

        private class UserNameChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        level.setValue(getUserLevel(allUsernames.getValue().toString()));
                }
        }
}
