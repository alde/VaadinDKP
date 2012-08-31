package com.unknown.entity.raids.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.raids.*;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

public class RaidEditWindow extends Window
{

    private final Raid raid;
    private List<RaidInfoListener> listeners = new ArrayList<RaidInfoListener>();
    private List<CharacterInfoListener> charlisten = new ArrayList<CharacterInfoListener>();
    private final DkpList dkplist;
    private final CharacterList clist;
    private RaidRewardList rrList;
    private RaidLootList rlList;
    private final ItemList itemList;
    private Application app;

    public RaidEditWindow(Raid raid, DkpList dkplist, CharacterList clist, ItemList itemList)
    {
        this.raid = raid;
        this.dkplist = dkplist;
        this.clist = clist;
        this.itemList = itemList;
        this.setPositionX(600);
        this.setPositionY(100);
        this.getContent().setSizeUndefined();
        this.addStyleName("opaque");
        this.setCaption("Edit raid: " + raid.getRaidname());
    }

    public void printInfo()
    {
        raidInformation();

        HorizontalLayout hzl = new HorizontalLayout();
        hzl.setSpacing(true);

        this.rrList = new RaidRewardList(raid, dkplist, clist);
        rrList.addStyleName("striped");
        hzl.addComponent(rrList);

        this.rlList = new RaidLootList(raid, dkplist, clist, itemList);
        rlList.addStyleName("striped");
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

    private void raidInformation()
    {
        List<String> zoneList = RaidDB.getRaidZoneList();

        HorizontalLayout hzl = new HorizontalLayout();
        final ComboBox zone = new ComboBox("Zone");
        for (String zones : zoneList) {
            zone.addItem(zones);
        }
        zone.setWidth("250px");
        zone.addStyleName("select-button");
        zone.setImmediate(true);
        zone.setNullSelectionAllowed(false);
        zone.setValue(raid.getRaidname());

        final TextField comment = new TextField("Comment: ", raid.getComment());
        comment.setImmediate(true);

        final TextField datum = new TextField("Date: ", raid.getDate());
        datum.setImmediate(true);

        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");

        hzl.addComponent(zone);
        hzl.addComponent(comment);
        hzl.addComponent(datum);
        hzl.setSpacing(true);

        addComponent(hzl);

        HorizontalLayout hoributtons = new HorizontalLayout();
        hoributtons.addComponent(updateButton);
        hoributtons.addComponent(deleteButton);
        hoributtons.setSpacing(true);
        addComponent(hoributtons);


        updateButton.addListener(new UpdateButtonListener(zone, comment, datum));
        deleteButton.addListener(new DeleteButtonListener());
    }

    private void deleteRaid()
    {

        RaidDB.safelyRemoveRaid(raid);
        notifyListeners();
        close();
    }

    private void updateRaid(String raidzoneName, String raidcomment, String raiddate)
    {

        RaidDB.doRaidUpdate(raid, raidzoneName, raidcomment, raiddate);
    }

    public void addRaidInfoListener(RaidInfoListener listener)
    {
        listeners.add(listener);
    }

    public void addCharacterInfoListener(CharacterInfoListener listener)
    {
        charlisten.add(listener);
    }

    private void notifyListeners()
    {
        for (RaidInfoListener raidListener : listeners) {
            raidListener.onRaidInfoChanged();
        }
        for (CharacterInfoListener charinfoListener : charlisten) {
            charinfoListener.onCharacterInfoChange();
        }
    }

    public void addApplication(Application app)
    {
        this.app = app;
    }

    private class UpdateButtonListener implements ClickListener
    {

        private final ComboBox zone;
        private final TextField comment;
        private final TextField datum;

        public UpdateButtonListener(ComboBox zone, TextField comment, TextField datum)
        {
            this.zone = zone;
            this.comment = comment;
            this.datum = datum;
        }

        @Override
        public void buttonClick(ClickEvent event)
        {
            final String raidzoneName = zone.getValue().toString();
            final String raidcomment = comment.getValue().toString();
            final String raiddate = datum.getValue().toString();
            updateRaid(raidzoneName, raidcomment, raiddate);
            notifyListeners();
        }
    }

    private class AddLootClickListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            RaidLootAddWindow rlootadd = new RaidLootAddWindow(raid);
            rlootadd.addCharacterInfoListener(dkplist);
            rlootadd.addCharacterInfoListener(clist);
            rlootadd.addRaidInfoListener(rlList);
            rlootadd.addItemInfoListener(itemList);
            rlootadd.printInfo();
            getApplication().getMainWindow().addWindow(rlootadd);
        }
    }

    private class AddRewardClickListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            RaidRewardAddWindow rewardadd = new RaidRewardAddWindow(raid);
            rewardadd.addCharacterInfoListener(dkplist);
            rewardadd.addCharacterInfoListener(clist);
            rewardadd.addRaidInfoListener(rrList);
            rewardadd.addApplication(app);
            rewardadd.printInfo();
            getApplication().getMainWindow().addWindow(rewardadd);
        }
    }

    private class DeleteButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            deleteRaid();
        }
    }
}
