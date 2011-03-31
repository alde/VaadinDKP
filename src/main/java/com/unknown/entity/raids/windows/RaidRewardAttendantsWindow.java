/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids.windows;

import com.unknown.entity.database.CharDB;
import com.unknown.entity.raids.RaidChar;
import com.unknown.entity.raids.RaidRewardListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
public class RaidRewardAttendantsWindow extends Window {

	private final List<RaidChar> chars;
        private List<RaidRewardListener> listeners = new ArrayList<RaidRewardListener>();

	public RaidRewardAttendantsWindow(List<RaidChar> chars) {
		this.chars = chars;
                this.setCaption("Attendants");
		this.setPositionX(500);
                this.setPositionY(250);
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
	}

	public void printInfo() {
		HorizontalLayout hzl = new HorizontalLayout();
		hzl.setSpacing(true);
		Table Attendants = charList();
                hzl.addComponent(getAttendants(Attendants));
		addComponent(hzl);
	}

	private Table charList() {
		Table tbl = new Table();
                tbl.addStyleName("small");
		raidCharWindowCharListSetHeaders(tbl);
		tbl.setHeight("270px");
                tbl.setWidth("180px");
		for (RaidChar rchar : chars) {
			Item addItem = tbl.addItem(rchar);
			raidCharWindowCharListAddRow(addItem, rchar);
		}
                tbl.addStyleName("striped");
		return tbl;
	}

        private void raidCharWindowCharListSetHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", Label.class, "");
                tbl.addContainerProperty("Shares", Integer.class, "");
        }

        private void raidCharWindowCharListAddRow(Item addItem, RaidChar rchar) throws ReadOnlyException, ConversionException {
                Label charname = new Label(rchar.getName());
                String charclass = CharDB.getRoleForCharacter(rchar.getName());
                charname.addStyleName(charclass.replace(" ", "").toLowerCase());
                addItem.getItemProperty("Name").setValue(charname);
                addItem.getItemProperty("Shares").setValue(rchar.getShares());
        }

        private Component getAttendants(Table attendants) {
                if (attendants.size() > 0) {
			return attendants;
		} else {
			return new Label("No members in this reward.");
		}
        }

        public void addRaidRewardInfoListener(RaidRewardListener listener) {
                listeners.add(listener);
        }
}
