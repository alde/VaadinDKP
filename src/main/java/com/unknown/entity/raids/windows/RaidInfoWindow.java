/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.raids.*;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
public class RaidInfoWindow extends Window {
        private List<RaidInfoListener> listeners = new ArrayList<RaidInfoListener>();
        private final Raid raid;

        public RaidInfoWindow(Raid raid) {
                this.raid = raid;
                this.setPositionX(600);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
                this.addStyleName("opaque");
                this.setCaption(raid.getName());

        }

        public void printInfo() {
                raidInformation();

                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);

                RaidRewardList rRewardList = new RaidRewardList(raid);
                hzl.addComponent(rRewardList);
                RaidLootList rLootList = new RaidLootList(raid);
                hzl.addComponent(rLootList);

                addComponent(hzl);
        }

        private void raidInformation() {
                addComponent(new Label("Raid information"));
                addComponent(new Label("Zone: " + raid.getName()));
                addComponent(new Label("Comment: " + raid.getComment()));
                addComponent(new Label("Date: " + raid.getDate()));
        }

        public void addRaidInfoListener(RaidInfoListener listener) {
                listeners.add(listener);
        }
}
