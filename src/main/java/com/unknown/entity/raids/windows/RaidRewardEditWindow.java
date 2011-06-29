
package com.unknown.entity.raids.windows;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.database.*;
import com.unknown.entity.raids.*;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;


public class RaidRewardEditWindow extends Window {

        private List<RaidRewardListener> listeners = new ArrayList<RaidRewardListener>();
        private List<CharacterInfoListener> charinfolisteners = new ArrayList<CharacterInfoListener>();
        private final List<RaidChar> chars;
        private final int shares;
        private final RaidReward reward;
        private final String oldcomment;
        private Application app;

        public RaidRewardEditWindow(RaidReward reward) {
                this.reward = reward;
                this.chars = reward.getRewardChars();
                this.shares = reward.getShares();
                this.oldcomment = reward.getComment();
                this.setPositionX(600);
                this.setPositionY(100);
                this.addStyleName("opaque");
                this.setCaption("Edit reward: " + reward.getComment());
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

        private void updateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) {
               
                RaidDB.doUpdateReward(reward, newAttendants, newShares, newComment);
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

        private void removeReward(RaidReward reward) {
               
                RaidDB.removeReward(reward);
        }

        private void showInvalidUsers(List<String> invalidchars) {
                addComponent(new Label("Invalid characters"));
                for (String s : invalidchars) {
                        addComponent(new Label(s));
                }
        }

        public void addApplication(Application app) {
                this.app = app;
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
                        List<String> invalidchars = RaidDB.findInvalidCharacters(attendantlist);
                        if (invalidchars.isEmpty()) {
                                updateReward(reward, attendantlist, newShares, newComment);
                        } else {
                                showInvalidUsers(invalidchars);
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

        public void addCharacterInfoListener(CharacterInfoListener lstnr) {
                charinfolisteners.add(lstnr);
        }

        private class RemoveButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        removeReward(reward);
                        notifyListeners();
                        close();
                }
        }

        private class CloseButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }
}
