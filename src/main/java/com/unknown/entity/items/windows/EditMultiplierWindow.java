/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.items.Multiplier;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.MultiplierList;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
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
public class EditMultiplierWindow extends Window {

        ItemDAO itemDao = null;
        List<Multiplier> multipliers = new ArrayList<Multiplier>();
        TextField itemlevel;
        TextField multiplier;
        MultiplierList multiplierTable;

        public EditMultiplierWindow() {
                this.setCaption("Edit multipliers");
                this.addStyleName("opaque");
                this.center();
                this.setResizable(false);
                this.getContent().setSizeUndefined();
                this.itemDao = new ItemDB();
        }

        public void printInfo() {
                GridLayout gl = new GridLayout(3, 2);
                this.itemlevel = new TextField();
                this.multiplier = new TextField();
                Button addButton = new Button("Add");
                addButton.addListener(new AddButtonListener());
                Label ilvl = new Label("Itemlevel");
                Label mtpl = new Label("Multiplier");
                gl.addComponent(ilvl, 0, 0);
                gl.addComponent(itemlevel, 0, 1);
                gl.addComponent(mtpl, 1, 0);
                gl.addComponent(multiplier, 1, 1);
                gl.addComponent(addButton, 2, 1);
                addComponent(gl);

                this.multiplierTable = new MultiplierList();
                addComponent(multiplierTable);

                Button updateButton = new Button("Update");
                updateButton.addListener(new UpdateButtonListener());
                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());

                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(updateButton);
                hzl.addComponent(closeButton);
                hzl.setSpacing(true);
                addComponent(hzl);
        }

        private void addItemlevel() {
                String mtp = multiplier.getValue().toString().replace(",", ".");
                itemDao.addMultiplier(Integer.parseInt(itemlevel.getValue().toString()), Double.parseDouble(mtp.toString()));
                multiplierTable.update();
                itemlevel.setValue("");
                multiplier.setValue("");
        }

        private class AddButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addItemlevel();
                }
        }

        private class UpdateButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        multiplierTable.updateIlvls();
                }
        }

        private class CloseButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }
}
