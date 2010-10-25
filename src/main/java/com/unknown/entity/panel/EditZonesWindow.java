/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.RaidDB;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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

        public EditZonesWindow() {
                this.setCaption("Edit default Prices");
                this.addStyleName("opaque");
                this.center();
                this.getContent().setSizeUndefined();
                this.raidDao = new RaidDB();
        }

        void printInfo() {
                List<String> zones = raidDao.getRaidZoneList();

                Label addZoneLabel = new Label("Add Zone");
                addComponent(addZoneLabel);
                SuperImmediateTextField zoneName = new SuperImmediateTextField("");
                zoneName.setImmediate(true);
                zoneName.setWidth("200px");
                Button addButton = new Button("Add Zone");
                addButton.addListener(new AddButtonClickListener(zoneName));
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

                updateButton.addListener(new UpdateZoneListener(zoneList));

                hzl = new HorizontalLayout();
                hzl.addComponent(zoneList);
                hzl.addComponent(deleteZone);
                hzl.addComponent(updateButton);

                addComponent(hzl);

                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());
                addComponent(closeButton);
        }

        private class CloseButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class UpdateZoneListener implements ClickListener {

                private final ComboBox zoneList;

                public UpdateZoneListener(ComboBox zoneList) {
                        this.zoneList = zoneList;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        raidDao.removeZone(zoneList.getValue().toString());
                }
        }

        private class AddButtonClickListener implements ClickListener {

                SuperImmediateTextField zoneName;

                public AddButtonClickListener(SuperImmediateTextField zoneName) {
                        this.zoneName = zoneName;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        raidDao.addZone(zoneName.getValue().toString());
                }
        }

     
}
