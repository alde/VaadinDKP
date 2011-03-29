/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.RaidInfoListener;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
class EditZonesWindow extends Window {

        private String oldZone = "";
        private ComboBox zoneList;
        private RaidDAO raidDao;
        private CheckBox deleteZone;
        private TextField zoneName;
        private List<RaidInfoListener> listeners = new ArrayList<RaidInfoListener>();
        private Application app;

        public EditZonesWindow() {
                this.setCaption("Edit Zones");
                this.addStyleName("opaque");
                this.center();
                this.getContent().setSizeUndefined();
                this.raidDao = new RaidDB();
        }

        public void printInfo() {
                List<String> zones = raidDao.getRaidZoneList();

                Label addZoneLabel = new Label("Add Zone");
                addComponent(addZoneLabel);
                zoneName = new TextField();
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
                zoneList.setNewItemsAllowed(true);

                zoneList.addListener(new ComboBox.ValueChangeListener() {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                                setOldZone();
                        }
                });

                this.deleteZone = new CheckBox("Delete");
                Button updateButton = new Button("Update Zone");

                updateButton.addListener(new UpdateZoneListener());

                hzl = new HorizontalLayout();
                hzl.addComponent(zoneList);
                hzl.addComponent(deleteZone);
                hzl.addComponent(updateButton);
                String defaultzone = raidDao.getZoneNameById(1);
                Label warning = new Label("Don't delete " + defaultzone + " or you'll fuck things up.");
                warning.addStyleName("error");

                addComponent(hzl);
                addComponent(warning);

                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());
                addComponent(closeButton);
        }

        private void setOldZone() {
                String oldzone = zoneList.getValue().toString();
                if (raidDao.isValidZone(oldzone)) {
                        this.oldZone = oldzone;
                }
        }

        private void removeZone() {
                raidDao.removeZone(zoneList.getValue().toString());
                update();
        }

        private void update() {
                this.removeAllComponents();
                this.printInfo();
                notifyListeners();
        }

        private void updateZone() {
                if (deleteZone.booleanValue()) {
                        removeZone();
                } else {
                        updateZoneName();
                }
        }

        private void addZone() {
                raidDao.addZone(zoneName.getValue().toString());
                update();
        }

        private void updateZoneName() {
                String newZone = zoneList.getValue().toString();
                raidDao.updateZoneName(oldZone, newZone);
                update();
        }

        public void addRaidInfoListener(RaidInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (RaidInfoListener raidListener : listeners) {
                        raidListener.onRaidInfoChanged();
                }
        }

        void addApplication(Application app) {
                this.app = app;
                raidDao.setApplication(app);
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
                        updateZone();
                }
        }

        private class AddButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addZone();
                }
        }
}
