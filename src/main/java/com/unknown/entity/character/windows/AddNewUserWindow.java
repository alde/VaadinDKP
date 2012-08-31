package com.unknown.entity.character.windows;

import com.unknown.entity.database.CharDB;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AddNewUserWindow extends Window
{

    final TextField username;
    final TextField password;
    final TextField passwordCheck;
    final ComboBox level;
    private Application app;

    public AddNewUserWindow()
    {
        this.username = new TextField("Username");
        this.password = new TextField("Password");
        this.passwordCheck = new TextField("Confirm Password");
        this.level = new ComboBox("Userlevel");
        password.setSecret(true);
        passwordCheck.setSecret(true);
        username.setImmediate(true);
        password.setImmediate(true);
        passwordCheck.setImmediate(true);
        this.setCaption("Add User");
        this.center();
        this.addStyleName("opaque");
        this.getContent().setSizeUndefined();
    }

    public void printInfo()
    {
        HorizontalLayout hzl = new HorizontalLayout();
        Button addButton = new Button("Add");
        Button cancelButton = new Button("Cancel");
        addButton.addListener(new AddButtonListener());
        cancelButton.addListener(new CancelButtonListener());

        level.addItem("Admin");
        level.addItem("SuperAdmin");
        level.setNullSelectionAllowed(false);
        level.setImmediate(true);

        addComponent(username);
        addComponent(password);
        addComponent(passwordCheck);
        addComponent(level);

        hzl.addComponent(addButton);
        hzl.addComponent(cancelButton);
        addComponent(hzl);

    }

    public void addApplication(Application app)
    {
        this.app = app;
    }

    private class CancelButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            close();
        }
    }

    private void addUser()
    {
        if (checkPassword()) {
            int userlevel = 1;
            if (level.getValue().toString().equals("Admin")) {
                userlevel = 1;
            } else if (level.getValue().toString().equals("SuperAdmin")) {
                userlevel = 2;
            }

            String hashedpassword = hashPassword(password.getValue().toString());
            CharDB.
                    addNewSiteUser(username.getValue().toString(), hashedpassword, userlevel);
        } else {
            Label err = new Label("Passwords must match!");
            err.addStyleName("error");
            getWindow().addComponent(err);
        }
    }

    public String hashPassword(String password)
    {
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

    private boolean checkPassword()
    {
        return password.getValue().equals(passwordCheck.getValue());
    }

    private class AddButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            addUser();
            close();
        }
    }
}
