/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.dao.*;
import com.unknown.entity.database.*;
import com.unknown.entity.raids.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
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
public class RaidRewardEditWindow extends Window {

        private List<RaidRewardListener> listeners = new ArrayList<RaidRewardListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();
        private final List<RaidChar> chars;
        private final int shares;
        private final RaidReward reward;
        private final String oldcomment;
        private final RaidDAO raidDao;
        private final CharacterDAO chardao;

        public RaidRewardEditWindow(RaidReward reward) {
                this.reward = reward;
                this.chars = reward.getRewardChars();
                this.shares = reward.getShares();
                this.oldcomment = reward.getComment();
                this.setPositionX(600);
                this.setPositionY(100);
                this.addStyleName("opaque");
                this.setCaption("Edit reward: " + reward.getComment());
                this.raidDao = new RaidDB();
                this.chardao = new CharacterDB();
                this.getContent().setSizeUndefined();
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

                final TextField comment = new TextField("Comment");
                comment.setValue(oldcomment);
                comment.setImmediate(true);
                vert.addComponent(comment);

                Button updateButton = new Button("Update");
                vert.addComponent(updateButton);
                updateButton.addListener(new UpdateButtonClickListener(attendants, share, comment));

                Button removeButton = new Button("Remove this reward");
                vert.addComponent(removeButton);
                removeButton.addListener(new RemoveButtonClickListener());

                Button closeButton = new Button("Close");
                vert.addComponent(closeButton);
                closeButton.addListener(new CloseButtonClickListener());

                hzl.addComponent(vert);
                addComponent(hzl);
        }

        private int updateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) throws SQLException {
                return raidDao.doUpdateReward(reward, newAttendants, newShares, newComment);
        }

        private TextField charList() {
                TextField characters = new TextField("Characters");
                characters.setRows(20);
                for (RaidChar character : chars) {
                        characters.setValue(characters.getValue().toString() + character.getName() + "\n");
                }
                return characters;
        }

         public void addRaidInfoListener(RaidRewardListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (RaidRewardListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
                for (CharacterInfoListener cinfoListener : charinfolisteners) {
                        cinfoListener.onCharacterInfoChange();
                }
        }

        private int removeReward(RaidReward reward) {
                try {
                        return raidDao.removeReward(reward);
                } catch (SQLException ex) {
                        Logger.getLogger(RaidRewardEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                return 0;
        }

        private void showInvalidUsers(List<String> invalidchars) {
                addComponent(new Label("Invalid characters"));
                for (String s : invalidchars) {
                        addComponent(new Label(s));
                }
        }

        private class UpdateButtonClickListener implements ClickListener {

                private final TextField attendants;
                private final TextField share;
                private final TextField comment;

                public UpdateButtonClickListener(TextField attendants, TextField share, TextField comment) {
                        this.attendants = attendants;
                        this.share = share;
                        this.comment = comment;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        final ImmutableList<String> attendantlist = splitCharsToArray(attendants.getValue().toString());
                        final int newShares = Integer.parseInt(share.getValue().toString());
                        String newComment = comment.getValue().toString();
                        try {
                                List<String> invalidchars = raidDao.findInvalidCharacters(attendantlist, chardao);
                                if (invalidchars.isEmpty()) {
                                        updateReward(reward, attendantlist, newShares, newComment);
                                        // System.out.println("Blaaaaaaaaaaaaa"+ reward.getComment());
                                } else {
                                        showInvalidUsers(invalidchars);
                                        // System.out.println(invalidchars.toString());
                                }

                        } catch (SQLException ex) {
                                Logger.getLogger(RaidRewardEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        notifyListeners();
                }

                private ImmutableList<String> splitCharsToArray(String attendants) {
                        String[] parts = attendants.split("\n");
                        return ImmutableList.of(parts);
                }
        }

        public void addRaidRewardInfoListener(RaidRewardListener listener) {
                listeners.add(listener);
        }
        public void addCharacterInfoListener(CharacterInfoListener lstnr)  {
                charinfolisteners.add(lstnr);
        }

        private class RemoveButtonClickListener implements ClickListener {

                public RemoveButtonClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        int success = removeReward(reward);
                        // System.out.println(success + " rewards removed.");
                        notifyListeners();
                        close();
                }
        }

        private class CloseButtonClickListener implements ClickListener {

                public CloseButtonClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }
}
