/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.SiteUser;
import com.unknown.entity.dao.ILoginDao;
import com.unknown.entity.dao.LoginDao;
import com.unknown.entity.panel.MyLoginListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginWindow extends Window {

        private TextField userName = new TextField("Username:");
        private TextField password = new TextField("Password");
        private Button submit = new Button("Login");
        private List<MyLoginListener> listeners = new ArrayList<MyLoginListener>();
        private List<CharacterInfoListener> charlisteners = new ArrayList<CharacterInfoListener>();
        private ILoginDao loginDao = new LoginDao();

        public LoginWindow() {
                this.center();
                this.setModal(true);
                this.setCaption("Login...");
                this.addStyleName("opaque");
                password.setSecret(true);
                addComponent(userName);
                userName.focus();
                addComponent(password);
                addComponent(submit);
                submit.setClickShortcut(KeyCode.ENTER);
                submit.addStyleName("primary");
                this.getContent().setSizeUndefined();

                submit.addListener(new LoginClickListener());

        }

        public void setLoginDao(ILoginDao loginDao) {
                this.loginDao = loginDao;
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

        private void verifyUser() {
                SiteUser user = loginDao.checkLogin(userName.getValue().toString(), hashPassword(password.getValue().toString()));
                if (user != null) {
                        getApplication().setUser(user);
                        notifyListeners();
                        close();
                }
        }

        public void addLoginListener(MyLoginListener listener) {
                listeners.add(listener);
        }

        private class LoginClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        verifyUser();
                }
        }

        private void notifyListeners() {
                for (MyLoginListener loginListener : listeners) {
                        loginListener.onLogin();
                }
                for (CharacterInfoListener characterListener : charlisteners) {
                        characterListener.onCharacterInfoChange();
                }
        }

        public void addCharacterInfoListener(CharacterInfoListener charlistener) {
                charlisteners.add(charlistener);
        }
}
