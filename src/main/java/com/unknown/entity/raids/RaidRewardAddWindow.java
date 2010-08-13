/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
class RaidRewardAddWindow extends Window {

        private Raid raid;

        public RaidRewardAddWindow(Raid raid) {
                this.raid = raid;
                this.setWidth("250px");
                this.setHeight("550px");
                this.setCaption("Add reward for raid " + raid.getComment() + " (id " + raid.getID() + ")");
        }

        public void printInfo() {
                final TextField attendants = new TextField("Attendants");
                attendants.setRows(20);
                attendants.setImmediate(true);

                TextField shares = new TextField("Shares");
                shares.setImmediate(true);

                final TextField comment = new TextField("Comment");
                comment.setImmediate(true);

                Button addButton = new Button("Add");

                VerticalLayout vert = new VerticalLayout();
                vert.addComponent(comment);
                vert.addComponent(shares);
                vert.addComponent(attendants);
                vert.addComponent(addButton);

                addComponent(vert);

                addButton.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                                final List<String> attendantlist = new ArrayList<String>();
                                attendantlist.addAll(splitCharsToArray(attendants.getValue().toString()));
                                addReward(comment.getValue().toString(), (Integer) shares.getValue(), attendantlist, raid);
                        }
                });
        }

        private List<String> splitCharsToArray(String attendants) {
                List<String> list = new ArrayList<String>();
                String[] parts = attendants.split("\n");
                for (String eachpart : parts)
                        list.add(eachpart);
                return list;
        }

}
