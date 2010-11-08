/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import java.util.List;
import org.vaadin.henrik.superimmediatetextfield.SuperImmediateTextField;

/**
 *
 * @author alde
 */
class EditZonesWindow extends Window {

        String oldZone = "";
        private ComboBox zoneList;
        private RaidDAO raidDao;
        SuperImmediateTextField zoneName;

        public EditZonesWindow() {
                this.setCaption("Edit Zones");
                this.addStyleName("opaque");
                this.center();
                this.getContent().setSizeUndefined();
                this.raidDao = new RaidDB();
        }

        void printInfo() {
                List<String> zones = raidDao.getRaidZoneList();

                Label addZoneLabel = new Label("Add Zone");
                addComponent(addZoneLabel);
                zoneName = new SuperImmediateTextField();
                zoneName.setImmediate(true);
                zoneName.setWidth("200px");
                zoneName.addStyleName("textfieldfont");
                Button addButton = new Button("Add Zone");
                addButton.addListener(new AddButtonClickListener());
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(zoneName);
                hzl.addComponent(addButton);
                addComponent(hzl);

                Label editZoneLabel = new Label("Zones");
                addComponent(editZoneLabel);
                this.zoneList = new ComboBox();
                zoneList.setWidth("200px");
                for (String zone : zones) {
                        zoneList.addItem(zone);
                }

                zoneList.setImmediate(true);

                CheckBox deleteZone = new CheckBox("Delete");
                Button updateButton = new Button("Delete Zone");

                updateButton.addListener(new UpdateZoneListener());

                hzl = new HorizontalLayout();
                hzl.addComponent(zoneList);
                hzl.addComponent(deleteZone);
                hzl.addComponent(updateButton);
                Label warning = new Label("Don't delete Old (Default) \nor you'll fuck things up.");
                warning.addStyleName("error");

                addComponent(hzl);
                addComponent(warning);

                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());
                addComponent(closeButton);
        }

        private void removeZone() {
                raidDao.removeZone(zoneList.getValue().toString());
                this.removeAllComponents();
                this.printInfo();
        }

        private void addZone() {
                raidDao.addZone(zoneName.getValue().toString());
                this.removeAllComponents();
                this.printInfo();
        }

        private class CloseButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class UpdateZoneListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        removeZone();
                }
        }

        private class AddButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addZone();
                }
        }
}
