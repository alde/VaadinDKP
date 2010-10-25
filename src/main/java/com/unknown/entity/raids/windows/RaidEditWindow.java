/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidEditWindow extends Window {

        private final Raid raid;
        private List<RaidInfoListener> listeners = new ArrayList<RaidInfoListener>();
        private final DkpList dkplist;
        private final CharacterList clist;
        private RaidRewardList rrList;
        private RaidLootList rlList;

        public RaidEditWindow(Raid raid, DkpList dkplist, CharacterList clist) {
                this.raid = raid;
                this.dkplist = dkplist;
                this.clist = clist;
                this.setPositionX(600);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.setCaption("Edit raid: " + raid.getName());
        }

        public void printInfo() {
                raidInformation();

                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);

                this.rrList = new RaidRewardList(raid, dkplist, clist);
                rrList.addStyleName("small striped");
                hzl.addComponent(rrList);

                this.rlList = new RaidLootList(raid, dkplist, clist);
                rlList.addStyleName("small striped");
                hzl.addComponent(rlList);

                Button addReward = new Button("Add Reward");
                addReward.addListener(new AddRewardClickListener());
                Button addLoot = new Button("Add loot");

                addLoot.addListener(new AddLootClickListener());

                addComponent(hzl);
                HorizontalLayout vert = new HorizontalLayout();
                vert.addComponent(addReward);
                vert.addComponent(addLoot);
                addComponent(vert);

        }

        private void raidInformation() {
                RaidDAO raidDAO = new RaidDB();
                List<String> zoneList = raidDAO.getRaidZoneList();

                HorizontalLayout hzl = new HorizontalLayout();
                addComponent(new Label("Raid information"));
                final ComboBox zone = new ComboBox("Zone");
                for (String zones : zoneList) {
                        zone.addItem(zones);
                }
                zone.setWidth("150px");
                zone.addStyleName("select-button");
                zone.setImmediate(true);
                zone.setNullSelectionAllowed(false);
                zone.setValue(raid.getName());

                final TextField comment = new TextField("Comment: ", raid.getComment());
                comment.setImmediate(true);


                final DateField datum = new DateField("Date");
                datum.setImmediate(true);
                Date date = new Date();
                datum.setDateFormat("yyyy-MM-dd HH:mm");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                datum.setValue(dateFormat.format(date));

//                final TextField datum = new TextField("Date: ", raid.getDate());
//                datum.setImmediate(true);

                Button updateButton = new Button("Update");

                hzl.addComponent(zone);
                hzl.addComponent(comment);
                hzl.addComponent(datum);

                addComponent(hzl);
                addComponent(updateButton);

                updateButton.addListener(new UpdateButtonListener(zone, comment, datum));
        }

        private int updateRaid(String raidzoneName, String raidcomment, String raiddate) throws SQLException {
                RaidDAO raidDao = new RaidDB();
                return raidDao.doRaidUpdate(raid, raidzoneName, raidcomment, raiddate);
        }

        public void addRaidInfoListener(RaidInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (RaidInfoListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
        }

        private class UpdateButtonListener implements ClickListener {

                private final ComboBox zone;
                private final TextField comment;
                private final DateField datum;

                public UpdateButtonListener(ComboBox zone, TextField comment, DateField datum) {
                        this.zone = zone;
                        this.comment = comment;
                        this.datum = datum;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        final String raidzoneName = zone.getValue().toString();
                        final String raidcomment = comment.getValue().toString();
                        final String raiddate = datum.getValue().toString();
                        try {
                                final int success = updateRaid(raidzoneName, raidcomment, raiddate);
                        } catch (SQLException ex) {
                                Logger.getLogger(RaidEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        notifyListeners();
                }
        }

        private class AddLootClickListener implements ClickListener {

                public AddLootClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        RaidLootAddWindow rlootadd = new RaidLootAddWindow(raid);
                        rlootadd.addCharacterInfoListener(dkplist);
                        rlootadd.addCharacterInfoListener(clist);
                        rlootadd.addRaidInfoListener(rlList);
                        try {
                                rlootadd.printInfo();
                        } catch (SQLException ex) {
                                Logger.getLogger(RaidEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        getApplication().getMainWindow().addWindow(rlootadd);
                }
        }

        private class AddRewardClickListener implements ClickListener {

                public AddRewardClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        RaidRewardAddWindow rewardadd = new RaidRewardAddWindow(raid);
                        rewardadd.addCharacterInfoListener(dkplist);
                        rewardadd.addCharacterInfoListener(clist);
                        rewardadd.addRaidInfoListener(rrList);
                        rewardadd.printInfo();
                        getApplication().getMainWindow().addWindow(rewardadd);
                }
        }
}
