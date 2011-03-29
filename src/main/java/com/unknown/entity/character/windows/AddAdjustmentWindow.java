/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.Adjustment;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alde
 */
class AddAdjustmentWindow extends Window {

        private List<CharacterInfoListener> listeners = new ArrayList<CharacterInfoListener>();
        private int userid;
        private TextField comment;
        private TextField shares;
        private DateField date;
        private Application app;

        public AddAdjustmentWindow(int userid) {
                this.setCaption("Add Adjustment");
                this.addStyleName("opaque");
                this.setPositionX(200);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
                this.userid = userid;
        }

        public void printInfo() {

                VerticalLayout addPunishment = new VerticalLayout();
                addComponent(addPunishment);
                this.comment = commentTextField();
                comment.setWidth("150px");
                addPunishment.addComponent(comment);

                dateTextField();
                date.setWidth("150px");
                addPunishment.addComponent(date);

                this.shares = sharesTextField();
                shares.setWidth("150px");
                addPunishment.addComponent(shares);

                Button btn = addPunishmentButton();
                Button cbtn = addPunishmentButtonClose();
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);
                hzl.addComponent(btn);
                hzl.addComponent(cbtn);
                addPunishment.addComponent(hzl);
        }

        public void addCharacterInfoListener(CharacterInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (CharacterInfoListener characterListener : listeners) {
                        characterListener.onCharacterInfoChange();
                }
        }

        private TextField commentTextField() {
                final TextField commentField = new TextField("Comment");
                commentField.setImmediate(true);
                commentField.focus();
                return commentField;
        }

        private void dateTextField() {
                date = new DateField("Date");
                date.setImmediate(true);
                Date dat = new Date();
                date.setDateFormat("yyyy-MM-dd HH:mm");
                date.setValue(dat);
        }

        private TextField sharesTextField() {
                final TextField sharesField = new TextField("Shares");
                sharesField.setImmediate(true);
                return sharesField;
        }

        private Button addPunishmentButton() {
                Button addbutton = new Button("Add");
                addbutton.addListener(new AddAdjustmentListener());
                return addbutton;
        }

        private Button addPunishmentButtonClose() {
                Button closebutton = new Button("Close");
                closebutton.addListener(new CloseListener());
                return closebutton;
        }

        void addApplication(Application app) {
                this.app = app;
        }

        private class CloseListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class AddAdjustmentListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        RaidDAO rdao = new RaidDB();
                        Adjustment p = new Adjustment();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        int share = Integer.parseInt(shares.getValue().toString());
                        p.setCharId(userid);
                        p.setComment(comment.getValue().toString());
                        p.setDate(dateFormat.format(date.getValue()));
                        p.setShares(share);
                        rdao.setApplication(app);
                        rdao.addAdjustment(p);
                        notifyListeners();
                        close();
                }
        }
}
