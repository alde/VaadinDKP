/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
class RaidRewardEditWindow extends Window {

        private final List<RaidChar> chars;
        private final int shares;
        private final RaidReward reward;

        RaidRewardEditWindow(RaidReward reward) {
                this.reward = reward;
                this.chars = reward.getRewardChars();
                this.shares = reward.getShares();
                this.setPositionX(600);
                this.setPositionY(200);
                this.setCaption("Edit reward: " + reward.getComment());
                setWidth("350px");
                setHeight("400px");
        }

        public void printInfo() {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);

                final TextField attendants = charList();
                hzl.addComponent(attendants);

                VerticalLayout vert = new VerticalLayout();
                final TextField share = new TextField("Shares");
                share.setValue(shares);
                share.setImmediate(true);
                vert.addComponent(share);

                Button updateButton = new Button("Update");
                vert.addComponent(updateButton);
                updateButton.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                                String chars[] = attendants.getValue().toString().split("\n");
                                List<String> newAttendants = new ArrayList<String>();
                                for (String s : chars) {
                                        newAttendants.add(s);
                                        System.out.println(s);
                                }
                                int newShares = Integer.parseInt(share.getValue().toString());
                                try {
                                        updateReward(reward, newAttendants, newShares);
                                } catch (SQLException ex) {
                                        Logger.getLogger(RaidRewardEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                });
                Button closeButton = new Button("Close");
                vert.addComponent(closeButton);
                closeButton.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                                close();
                        }
                });

                hzl.addComponent(vert);
                addComponent(hzl);
        }

        private int updateReward(RaidReward reward, List<String> newAttendants, int newShares) throws SQLException {
                RaidDAO raidDao = new RaidDB();
                return raidDao.doUpdateReward(reward, newAttendants, newShares);
        }

        private TextField charList() {
                TextField characters = new TextField("Characters");
                characters.setRows(20);
                for (RaidChar character : chars) {
                        characters.setValue(characters.getValue().toString() + character.getName() + "\n");
                }
                return characters;
        }
}
