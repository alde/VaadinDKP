/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character.windows;

import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.User;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.Raid;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.sql.SQLException;

/**
 *
 * @author bobo
 */
public class CharacterInfoWindow extends Window {

        private RaidDAO raidDao;
        private final User user;

        public CharacterInfoWindow(User user) {
                this.user = user;
                this.raidDao = new RaidDB();
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
                addItem.getItemProperty("Name").setValue(charitem.getName());
                addItem.getItemProperty("Price").setValue(charitem.getPrice());
                addItem.getItemProperty("Heroic").setValue(charitem.getHeroic());
        }

        private void characterInfoLootTableSetHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", String.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.addContainerProperty("Heroic", Boolean.class, false);
        }

        private void characterInformation() {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Name: "));
                Label charname = new Label(user.getUsername());
                charname.addStyleName("color");
                hzl.addComponent(charname);
                addComponent(hzl);
                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Class: "));
                Label charclass = new Label(user.getRole().toString());
                charclass.addStyleName("color");
                hzl.addComponent(charclass);
                addComponent(hzl);
                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Status: "));
                Label charactive = new Label((user.isActive() ? "Active" : "Inactive"));
                charactive.addStyleName("color");
        }

        private void characterLoots() {
                Table loots = lootList(user);
                addComponent(new Label("Loots"));
                if (loots.size() > 0) {
                        addComponent(lootList(user));
                } else {
                        addComponent(new Label("No items looted yet."));
                }
        }

        private void characterRaids() throws SQLException {
                Table raids = raidList(user);
                addComponent(new Label("Raids"));
                if (raids.size() > 0) {
                        addComponent(raidList(user));
                } else {
                        addComponent(new Label("No raids attended yet."));
                }
        }

        private void characterDKP() throws OutOfBoundsException, OverlapsException {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("Shares: "));
                Label shares = new Label(""+user.getShares());
                shares.addStyleName("color");
                hzl.addComponent(shares);
                addComponent(hzl);
                
                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Earned: "));
                Label dkpearned = new Label(""+user.getDKPEarned());
                dkpearned.addStyleName("color");
                hzl.addComponent(dkpearned);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP Spent: "));
                Label dkpspent = new Label(""+user.getDKPSpent());
                dkpspent.addStyleName("color");
                hzl.addComponent(dkpspent);
                addComponent(hzl);

                hzl = new HorizontalLayout();
                hzl.setWidth("200px");
                hzl.addComponent(new Label("DKP: "));
                Label dkp = new Label(""+user.getDKP());
                dkp.addStyleName("color");
                hzl.addComponent(dkp);
                addComponent(hzl);
        }

        private Table lootList(User user) {
                Table tbl = new Table();
                characterInfoLootTableSetHeaders(tbl);
                tbl.setHeight("150px");
                for (CharacterItem charitem : user.getCharItems()) {
                        Item addItem = tbl.addItem(charitem.getId());
                        characterInfoLootTableAddRow(addItem, charitem);
                }
                return tbl;
        }

        private Table raidList(User user) throws SQLException {
                Table tbl = new Table();
                tbl.addContainerProperty("Comment", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
                tbl.setHeight("150px");
                for (Raid charraid : raidDao.getRaidsForCharacter(user.getId())) {
                        Item addItem = tbl.addItem(charraid);
                        addItem.getItemProperty("Comment").setValue(charraid.getComment());
                        addItem.getItemProperty("Date").setValue(charraid.getDate());
                }
                return tbl;
        }

        private void raidsAttended() {
                CharacterDAO charDao = new CharacterDB();
                String attendance = charDao.getAttendanceRaids(user);
                Label attended = new Label();
                attended.setValue("Attended " + attendance +"% of raids the last 30 days.");
                addComponent(attended);
        }
}
