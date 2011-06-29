
package com.unknown.entity.character.windows;

import com.unknown.entity.character.Adjustment;
import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.User;
import com.unknown.entity.database.*;
import com.unknown.entity.items.*;
import com.unknown.entity.raids.*;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CharacterInfoWindow extends Window {
        private final User user;
        private Application app;
        private ItemList itemList;
        private RaidList raidList;
        private Table loots;
        private int longest;

        public CharacterInfoWindow(User user, Application app, RaidList raidList, ItemList itemList) {
                this.user = user;
                this.app = app;
                this.raidList = raidList;
                this.itemList = itemList;
                this.longest = 1;
                this.addStyleName("opaque");
                this.setCaption("Character information: " + user.getUsername());
                this.setPositionX(200);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() throws SQLException {
                characterInformation();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterDKP();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterLoots();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                characterRaids();
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                raidsAttended();
        }

        private void characterInfoLootTableAddRow(Item addItem, CharacterItem charitem) throws ReadOnlyException, ConversionException {
                Label itemname = new Label(charitem.getName());
                itemname.addStyleName(charitem.getQuality().toLowerCase());
                String slot = ItemDB.getSlotForItemByName(itemname.getValue().toString());
                addItem.getItemProperty("Name").setValue(itemname);
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
                addItem.getItemProperty("Slot").setValue(slot);
                if (charitem.getHeroic()) {
                        addItem.getItemProperty("Heroic").setValue("Yes");
                } else {
                        addItem.getItemProperty("Heroic").setValue("No");
                }

        }

        private void characterInfoLootTableSetHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", Label.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.addContainerProperty("Slot", String.class, "");
                tbl.addContainerProperty("Heroic", String.class, false);
        }

        private void characterInformation() {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Name: "));
                Label charname = new Label(user.getUsername());
                charname.addStyleName(user.getRole().toString().replace(" ", "").toLowerCase());
                hzl.addComponent(charname);
                addComponent(hzl);
                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Class: "));
                Label charclass = new Label(user.getRole().toString());
                charclass.addStyleName(user.getRole().toString().replace(" ", "").toLowerCase());
                hzl.addComponent(charclass);
                addComponent(hzl);
                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Status: "));
                Label charactive = new Label((user.isActive() ? "Active" : "Inactive"));
                charactive.addStyleName("color");
        }

        private void characterLoots() {
                loots = lootList(user);
                addComponent(new Label("Loots"));
                if (loots.size() > 0) {
                        loots.addStyleName("striped");
                        addComponent(lootList(user));
                } else {
                        addComponent(new Label("No items looted yet."));
                }
        }

        private void characterRaids() throws SQLException {
                HorizontalLayout hraid = new HorizontalLayout();
                Table raids = raidList(user);
                raids.setCaption("Raids");
                if (raids.size() > 0) {
                        raids.addStyleName("striped");
                        hraid.addComponent(raids);
                } else {
                        hraid.addComponent(new Label("No raids attended yet."));
                }
                Table punishments = punishmentList(user);
                punishments.setCaption("Adjustments");
                if (punishments.size() > 0) {
                        punishments.addStyleName("striped");
                        hraid.addComponent(punishments);
                } else {
                        hraid.addComponent(new Label("No adjustments."));
                }
                addComponent(hraid);
                hraid.setSpacing(true);
        }

        private void characterDKP() throws OutOfBoundsException, OverlapsException {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Shares: "));
                Label shares = new Label("" + user.getShares());
                shares.addStyleName("color");
                hzl.addComponent(shares);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Earned: "));
                Label dkpearned = new Label("" + user.getDKPEarned());
                dkpearned.addStyleName("color");
                hzl.addComponent(dkpearned);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Spent: "));
                Label dkpspent = new Label("" + user.getDKPSpent());
                dkpspent.addStyleName("color");
                hzl.addComponent(dkpspent);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP: "));
                Label dkp = new Label("" + user.getDKP());
                if (user.getDKP() >= 0) {
                        dkp.addStyleName("positive");
                } else {
                        dkp.addStyleName("negative");
                }
                hzl.addComponent(dkp);
                addComponent(hzl);
        }

        private Table lootList(User user) {
                Table tbl = new Table();
                characterInfoLootTableSetHeaders(tbl);
                tbl.setSizeUndefined();
                tbl.setHeight("200px");
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = tbl.addItem(charitem);
                        characterInfoLootTableAddRow(addItem, charitem);
                        if (longest < charitem.getName().length() + 1) {
                                longest = charitem.getName().length() + 1;
                        }
                        tbl.setColumnWidth("Name", longest * 6);
                        tbl.requestRepaint();
                }
                tbl.setSizeUndefined();
                tbl.addListener(new LootListClickListener());
                return tbl;
        }

        private Table raidList(User user) throws SQLException {
                Table tbl = new Table();
                tbl.addContainerProperty("Comment", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
                tbl.setSizeUndefined();
                tbl.setHeight("150px");

                List<Raid> cRaids = RaidDB.getRaidsForCharacter(user.getId());
                Collections.sort(cRaids, new CompareRaidDates());
                for (Raid charraid : cRaids) {
                        Item addItem = tbl.addItem(charraid);
                        addItem.getItemProperty("Comment").setValue(charraid.getComment());
                        addItem.getItemProperty("Date").setValue(charraid.getDate());
                }
                tbl.addListener(new RaidListClickListener());
                if (tbl.getWidth() < 500) {
                        tbl.setWidth("500px");
                }
                return tbl;
        }

        private Table punishmentList(User user) throws SQLException {
                Table tbl = new Table();
                tbl.addContainerProperty("Comment", String.class, "");
                tbl.addContainerProperty("Shares", Integer.class, 0);
                tbl.addContainerProperty("Date", String.class, "");
                tbl.setSizeUndefined();
                tbl.setHeight("150px");

                List<Adjustment> cPun = RaidDB.getAdjustmentsForCharacter(user.getId());
                Collections.sort(cPun, new CompareAdjustmentDates());
                for (Adjustment pun : cPun) {
                        Item addItem = tbl.addItem(pun);
                        addItem.getItemProperty("Comment").setValue(pun.getComment());
                        addItem.getItemProperty("Shares").setValue(pun.getShares());
                        addItem.getItemProperty("Date").setValue(pun.getDate());
                }
                tbl.addListener(new RaidListClickListener());
                if (tbl.getWidth() < 500) {
                        tbl.setWidth("500px");
                }
                return tbl;
        }

        private void raidsAttended() {
                Double attendance = user.getAttendance();
                Label attended = new Label();
                attended.setValue("Attended " + attendance + "% of raids the last 60 days.");
                if (attendance >= 0 && attendance < 50) {
                        attended.setStyleName("negative");
                } else if (attendance > 50 && attendance < 65) {
                        attended.setStyleName("uncommon");
                } else if (attendance >= 65 && attendance < 75) {
                        attended.setStyleName("rare");
                } else if (attendance >= 75 && attendance < 90) {
                        attended.setStyleName("epic");
                } else if (attendance >= 90) {
                        attended.setStyleName("legendary");
                }
                addComponent(attended);
        }

        private class LootListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {

                        if (event.isDoubleClick()) {
                                CharacterItem citem = (CharacterItem) event.getItemId();
                                Items temp = ItemDB.getSingleItem(citem.getName());
                                PopUpControl pop = new PopUpControl(app);
                                pop.setItemList(itemList);
                                pop.showProperItemWindow(temp);
                        }
                }
        }

        private class RaidListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isDoubleClick()) {
                                Raid item = (Raid) event.getItemId();
                                PopUpControl pop = new PopUpControl(app);
                                pop.setRaidList(raidList);
                                pop.showProperRaidWindow(item);
                        }
                }
        }

        private class CompareRaidDates implements Comparator<Raid> {

                @Override
                public int compare(Raid t, Raid t1) {
                        return t1.getDate().compareTo(t.getDate());
                }
        }

        private class CompareAdjustmentDates implements Comparator<Adjustment> {
                @Override
                public int compare(Adjustment t, Adjustment t1) {
                        return t1.getDate().compareTo(t.getDate());
                }
        }
}
