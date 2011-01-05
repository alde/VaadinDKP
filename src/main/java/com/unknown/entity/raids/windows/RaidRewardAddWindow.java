/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidChar;
import com.unknown.entity.raids.RaidReward;
import com.unknown.entity.raids.RaidRewardListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidRewardAddWindow extends Window {

        private Raid raid;
        private final TextField attendants = new TextField("Attendants");
        private final TextField shares = new TextField("Shares");
        private final TextField comment = new TextField("Comment");
        VerticalLayout vertError;
        Button addButton = new Button("Add");
        RaidDAO raidDao = new RaidDB();
        CharacterDAO chardao = new CharacterDB();
        private List<RaidRewardListener> listeners = new ArrayList<RaidRewardListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();

        public RaidRewardAddWindow(Raid raid) {
                this.raid = raid;
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.setCaption("Add reward for raid " + raid.getComment() + " (id " + raid.getId() + ")");
                this.vertError = new VerticalLayout();
                attendants.setRows(20);
                attendants.setImmediate(true);
                shares.setImmediate(true);
                comment.setImmediate(true);
                this.setPositionX(600);
                this.setPositionY(300);

        }

        public void printInfo() {
                VerticalLayout vert = new VerticalLayout();
                vert.addComponent(comment);
                vert.addComponent(shares);
                vert.addComponent(attendants);
                vert.addComponent(addButton);
                vert.addComponent(vertError);
                addComponent(vert);
                addButton.addListener(new AddRewardListener());
        }

        public void addRaidInfoListener(RaidRewardListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (RaidRewardListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
                for (CharacterInfoListener charListener : charinfolisteners) {
                        charListener.onCharacterInfoChange();
                }
        }

        void addCharacterInfoListener(CharacterInfoListener listener) {
                charinfolisteners.add(listener);
        }

        private void addReward(String comment, Integer shares, List<String> attendantlist, Raid raid) {
                List<String> invalidchars = findInvalidCharacters(attendantlist);
                if (invalidchars.isEmpty()) {
                        RaidReward raidReward = new RaidReward(comment, 0, raid.getId(), shares);
                        Collection<RaidChar> chars = raidDao.getRaidCharsForRaid(attendantlist, raid.getId());

                        raidReward.addRewardChars(chars);
                        System.out.println(raidReward.getRewardChars().toString());
                        raidDao.addReward(raidReward);
                        updateReward(raidReward, attendantlist, shares, comment);
                } else {
                        showInvalidUsers(invalidchars);
                }
        }

        private int updateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) {
                return raidDao.doUpdateReward(reward, newAttendants, newShares, newComment);
        }

        private void showInvalidUsers(List<String> invalidchars) {
                vertError.addComponent(new Label("Invalid characters"));
                for (String s : invalidchars) {
                        vertError.addComponent(new Label(s));
                }
        }

        private List<String> findInvalidCharacters(List<String> attendantlist) {
                List<String> invalid = new ArrayList<String>(attendantlist);
                invalid.removeAll(chardao.getUserNames());
                return ImmutableList.copyOf(invalid);
        }

        private class AddRewardListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        vertError.removeAllComponents();
                        final ImmutableList<String> attendantlist = splitCharsToArray(attendants.getValue().toString());
                        addReward(comment.getValue().toString(), Integer.parseInt(shares.getValue().toString()), attendantlist, raid);
                        notifyListeners();
                        close();
                }

                private ImmutableList<String> splitCharsToArray(String attendants) {
                        String[] parts = attendants.split("\n");
                        return ImmutableList.of(parts);
                }
        }
}
