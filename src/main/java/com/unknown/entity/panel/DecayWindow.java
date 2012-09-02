/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.character.Adjustment;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.items.ItemInfoListener;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidInfoListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class DecayWindow extends Window
{

    private Label message;
    private List<ItemInfoListener> itemListeners = new ArrayList<ItemInfoListener>();
    private List<CharacterInfoListener> charListeners = new ArrayList<CharacterInfoListener>();
    private List<RaidInfoListener> raidListeners = new ArrayList<RaidInfoListener>();
    private double percentDecay;
    private double percentDecay2;

    public DecayWindow()
    {
        this.setWidth("400px");
        this.setHeight("400px");
        this.setPositionX(200);
        this.setPositionY(100);
        this.setCaption("Decay shares from Raids and Adjustments");

        percentDecay = 50d;
        percentDecay2 = 90d;
    }

    void printInfo()
    {
        this.message = new Label();
        String decay_one = "";
        String decay_two = "";
        try {
            HashMap<String, Integer> counts = RaidDB.
                    countRewardsAndAdjustmentsToBeDecayed();
            decay_one = "(" + rewardNotice(counts.get("rewards_mid")) + " and "
                    + adjustmentNotice(counts.get("adjustments_mid"))
                    + " will be affected.)";
            decay_two = "(" + rewardNotice(counts.get("rewards_old")) + " and "
                    + adjustmentNotice(counts.get("adjustments_old"))
                    + " will be affected.)";
        } catch (ParseException ex) {
            Logger.getLogger(DecayWindow.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        Label notice_two_months = new Label(decay_one);
        Label notice_four_months = new Label(decay_two);
        notice_two_months.addStyleName("small");
        notice_four_months.addStyleName("small");

        TextField decay_over_two_months = new TextField(
                "Older than 2 months by (value in %)");
        decay_over_two_months.
                setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
        decay_over_two_months.setValue(percentDecay);
        decay_over_two_months.setWidth("50px");
        decay_over_two_months.setImmediate(true);
        decay_over_two_months.addListener(new DecayPercentListener());

        TextField decay_over_four_months = new TextField(
                "Older than 4 months by (value in %)");
        decay_over_four_months.
                setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
        decay_over_four_months.setValue(percentDecay2);
        decay_over_four_months.setWidth("50px");
        decay_over_four_months.setImmediate(true);
        decay_over_four_months.addListener(new DecayPercentListener2());

        Label notice = new Label(
                "Decay is automatically calculated from the original shares gained.");
        notice.addStyleName("notice");

        Button decayButton = new Button("Perform Decay");
        decayButton.addListener(new DecayButtonListener());
        Button closeButton = new Button("Close");
        closeButton.addListener(new CloseButtonListener());
        closeButton.addStyleName("button_close_decay");

        Button undoDecayButton = new Button("Restore Original Shares");
        undoDecayButton.addListener(new UndoDecayButtonListener());

        HorizontalLayout buttonHoriz = new HorizontalLayout();
        buttonHoriz.addComponent(decayButton);
        buttonHoriz.addComponent(undoDecayButton);
        buttonHoriz.addComponent(closeButton);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(decay_over_two_months);
        layout.addComponent(notice_two_months);
        layout.addComponent(decay_over_four_months);
        layout.addComponent(notice_four_months);
        layout.addComponent(message);
        layout.addComponent(buttonHoriz);
        layout.addComponent(notice);

        this.addComponent(layout);
    }

    public void addRaidInfoListener(RaidInfoListener listener)
    {
        raidListeners.add(listener);
    }

    public void addItemInfoListener(ItemInfoListener listener)
    {
        itemListeners.add(listener);
    }

    public void addCharacterInfoListener(CharacterInfoListener clistn)
    {
        charListeners.add(clistn);
    }

    private void notifyListeners()
    {
        for (ItemInfoListener itemInfoListener : itemListeners) {
            itemInfoListener.onItemInfoChange();
        }
        for (CharacterInfoListener charInfoListener : charListeners) {
            charInfoListener.onCharacterInfoChange();
        }
        for (RaidInfoListener raidInfoListener : raidListeners) {
            raidInfoListener.onRaidInfoChanged();
        }
    }

    private void undoDecay()
    {
        try {
            RaidDB.restoreRewards();
            RaidDB.restoreAdjustments();
            message.setValue("Decay undone.");
            message.setStyleName("success");
        } catch (SQLException ex) {
            message.setValue("Failed to undo decay: \n" + ex.
                    getLocalizedMessage());
            message.setStyleName("error");
            ex.printStackTrace();
        }
    }

    private void performDecay()
    {
        try {
            int count_rewards = 0;
            count_rewards += RaidDB.decayRaid(percentDecay, percentDecay2);

            int count_adjustments = 0;
            count_adjustments += RaidDB.
                    decayAdjustments(percentDecay, percentDecay2);

            message.
                    setValue(rewardNotice(count_rewards) + " and " + adjustmentNotice(count_adjustments) + " successfully updated.");
            message.setStyleName("success");
        } catch (ParseException ex) {
            Logger.getLogger(DecayWindow.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    private String rewardNotice(int get)
    {
        if (get > 1) {
            return get + " rewards";
        }
        if (get == 1) {
            return get + " reward";
        }
        return "no rewards";
    }

    private String adjustmentNotice(int get)
    {
        if (get > 1) {
            return get + " adjustments";
        }
        if (get == 1) {
            return get + " adjustment";
        }
        return "no adjustments";
    }

    private class CloseButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            close();
        }
    }

    private class DecayButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            message.setValue("");
            message.setStyleName("");
            performDecay();
            notifyListeners();
        }
    }

    private class DecayPercentListener implements ValueChangeListener
    {

        @Override
        public void valueChange(ValueChangeEvent event)
        {
            percentDecay = Double.parseDouble(event.getProperty().getValue().
                    toString());
        }
    }

    private class DecayPercentListener2 implements ValueChangeListener
    {

        @Override
        public void valueChange(ValueChangeEvent event)
        {
            System.out.println(event.getProperty().getValue().toString());
            percentDecay2 = Double.parseDouble(event.getProperty().getValue().
                    toString());
        }
    }

    private class UndoDecayButtonListener implements ClickListener
    {

        @Override
        public void buttonClick(ClickEvent event)
        {
            message.setValue("");
            message.setStyleName("");
            undoDecay();
            notifyListeners();
        }
    }
}
