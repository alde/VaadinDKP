/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.database.*;
import com.unknown.entity.*;
import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.dao.*;
import com.unknown.entity.items.*;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class ItemEditWindow extends Window {

        private Items item;
        private List<ItemInfoListener> listeners = new ArrayList<ItemInfoListener>();
        private ItemDAO itemDao;
//        private IndexedContainer ic;
        final TextField price;
        final TextField pricehc;
        Table lootedby;
        Table tbl;
        private List<CharacterInfoListener> charlisteners = new ArrayList<CharacterInfoListener>();
        private ItemLooterTable ilt;
        private TextField name;
        private ComboBox slot;
        private ComboBox type;
        private TextField wowIdField;
        private TextField wowIdFieldhc;
        private VerticalLayout lout;
        private TextField ilvl;

        public ItemEditWindow(Items item) {
                this.item = item;
                this.addStyleName("opaque");
                this.setCaption("Edit item: " + item.getName());
                this.setPositionX(400);
                this.setPositionY(50);
                this.getContent().setSizeUndefined();
                this.itemDao = new ItemDB();
                this.ilt = new ItemLooterTable(item);
                this.price = new TextField();
                this.pricehc = new TextField();
                this.slot = new ComboBox("Slot");
                this.type = new ComboBox("Type");
                this.wowIdField = new TextField();
                this.wowIdFieldhc = new TextField();
                this.name = new TextField("Name");
                this.ilvl = new TextField("Itemlevel");
                this.lout = new VerticalLayout();
        }

        public void printInfo() {
                addComponent(new Label("Item information"));
                editInfoName();
                editInfoSlot();
                editInfoType();
                editInfoPriceField();
                editInfoPriceHcField();
                editInfoWowIdField();
                editInfoWowIdHcField();
                editInfoIlvlField();

                addComponent(name);
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(slot);
                hzl.addComponent(type);
                hzl.addComponent(ilvl);
                hzl.setSpacing(true);
                addComponent(hzl);

                itemEditGrid(wowIdField, wowIdFieldhc, price, pricehc);

                Button updateButton = new Button("Update Item");
                Button deleteButton = new Button("Delete Item");
                Button requestButton = new Button("Request Default Prices");
                Button applyPrice = new Button("Apply Price");
                applyPrice.setDescription("Apply price to all loots of this item.");
                deleteButton.addListener(new DeleteButtonClickListener(item));
                updateButton.addListener(new UpdateButtonClickListener());
                requestButton.addListener(new RequestButtonClickListener());
                applyPrice.addListener(new ApplyButtonClickListener());
                hzl = new HorizontalLayout();
                Label warning = new Label();
                warning.setWidth("220px");
                warning.setValue("Can NOT be reverted.");
                warning.addStyleName("error");
                hzl.addComponent(updateButton);
                hzl.addComponent(deleteButton);
                hzl.addComponent(warning);
                hzl.addComponent(requestButton);
                hzl.addComponent(applyPrice);
                hzl.setSpacing(true);
                hzl.setMargin(true, false, true, false);
                addComponent(hzl);
                lout.addComponent(ilt);
                addComponent(lout);
                try {
                        itemTooltips(item);
                } catch (IOException ex) {
                        Logger.getLogger(ItemEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private void setPricesToDefault() {
                int itemlvl = Integer.parseInt(ilvl.getValue().toString());
                double prices = itemDao.getDefaultPrice(item);
                Multiplier mp = itemDao.getMultiplierForItemlevel(itemlvl);
                BigDecimal formattedprice = new BigDecimal(prices * mp.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                price.setValue("" + formattedprice);
                mp = itemDao.getMultiplierForItemlevel(itemlvl + 13);
                BigDecimal formattedpricehc = new BigDecimal(prices * mp.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                pricehc.setValue("" + formattedpricehc);
        }

        private void itemEditGrid(final TextField wowIdfield, final TextField wowIdfieldhc, final TextField price, final TextField pricehc) throws OutOfBoundsException, OverlapsException {
                GridLayout gl = new GridLayout(3, 3);
                gl.setWidth("500px");

                Link normal = new Link("Normal", new ExternalResource("http://www.wowhead.com/item=" + item.getWowId()));
                normal.setTargetName("_blank");
                Link heroic = new Link("Heroic", new ExternalResource("http://www.wowhead.com/item=" + item.getWowId_hc()));
                heroic.setTargetName("_blank");

                gl.addComponent(normal, 1, 0);
                gl.addComponent(heroic, 2, 0);
                gl.addComponent(new Label("WowID: "), 0, 1);

                gl.addComponent(wowIdfield, 1, 1);
                gl.addComponent(wowIdfieldhc, 2, 1);

                gl.addComponent(new Label("Price: "), 0, 2);
                gl.addComponent(price, 1, 2);
                gl.addComponent(pricehc, 2, 2);
                addComponent(gl);
        }

        private void editInfoPriceHcField() throws ReadOnlyException, ConversionException {
                pricehc.setImmediate(true);
                pricehc.setValue(item.getPrice_hc());
        }

        private void editInfoPriceField() throws ReadOnlyException, ConversionException {
                price.setImmediate(true);
                price.setValue(item.getPrice());
        }

        private void editInfoIlvlField() throws ReadOnlyException, ConversionException {
                ilvl.setImmediate(true);
                ilvl.setValue(item.getIlvl());
        }

        private void editInfoWowIdHcField() throws ConversionException, ReadOnlyException {
                wowIdFieldhc.setImmediate(true);
                wowIdFieldhc.setValue("" + item.getWowId_hc());
        }

        private void editInfoWowIdField() throws ReadOnlyException, ConversionException {
                wowIdField.setImmediate(true);
                wowIdField.setValue("" + item.getWowId());
        }

        private void editInfoType() throws ConversionException, ReadOnlyException, UnsupportedOperationException {
                for (Type types : Type.values()) {
                        type.addItem(types);
                }
                type.setNullSelectionAllowed(false);
                type.setValue(item.getType());
                type.setWidth("150px");
                type.addStyleName("select-button");
                type.setImmediate(true);
        }

        private void editInfoSlot() throws UnsupportedOperationException, ConversionException, ReadOnlyException {
                for (Slots slots : Slots.values()) {
                        slot.addItem(slots);
                }
                slot.setNullSelectionAllowed(false);
                slot.setValue(Slots.valueOf(item.getSlot().replace("-", "")));
                slot.setWidth("150px");
                slot.addStyleName("select-button");
                slot.setImmediate(true);
        }

        private void editInfoName() {
                name.setValue(item.getName());
                name.setWidth("300px");
                name.setImmediate(true);
        }

        private void updateItem() {
                String temp = type.getValue().toString();
                Type tempType = typeFromString(temp);
                int success = itemDao.updateItem(item, name.getValue().toString(), Slots.valueOf(slot.getValue().toString()), tempType, Integer.parseInt(wowIdField.getValue().toString()), Integer.parseInt(wowIdFieldhc.getValue().toString()), Double.parseDouble(price.getValue().toString()), Double.parseDouble(pricehc.getValue().toString()), Integer.parseInt(ilvl.getValue().toString()));
                System.out.println("Items updated: " + success);
                notifyListeners();
        }

        private Type typeFromString(String temp) {
                Type tempType;
                if (temp.toString().equals("Hunter, Shaman, Warrior")) {
                        tempType = Type.protector;
                } else if (temp.toString().equals("Death Knight, Druid, Mage, Rogue")) {
                        tempType = Type.vanquisher;
                } else if (temp.toString().equals("Paladin, Priest, Warlock")) {
                        tempType = Type.conqueror;
                } else {
                        tempType = Type.valueOf(temp);
                }
                return tempType;
        }

        private void applyPrices() {
                itemDao.updateLoots(item, price.getValue().toString(), pricehc.getValue().toString());
                lout.removeComponent(ilt);
                ilt = new ItemLooterTable(itemDao.getSingleItem(name.getValue().toString()));
                lout.addComponent(ilt);
                notifyListeners();
        }

        private int deleteItem(Items item) throws SQLException {
                return itemDao.deleteItem(item.getId());
        }

        public void addItemInfoListener(ItemInfoListener listener) {
                listeners.add(listener);
        }

        public void addCharacterInfoListener(CharacterInfoListener charlistener) {
                charlisteners.add(charlistener);
        }

        private void notifyListeners() {
                for (ItemInfoListener itemInfoListener : listeners) {
                        itemInfoListener.onItemInfoChange();
                }
                for (CharacterInfoListener charInfoListener : charlisteners) {
                        charInfoListener.onCharacterInfoChange();
                }
        }

        private void itemTooltips(Items item) throws IOException {
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
                HorizontalLayout hzl = new HorizontalLayout();
                XmlParser xml = new XmlParser("" + item.getWowId());
                String normalTooltip = xml.parseXmlTooltip();
                normalTooltip = normalTooltip.replace("href", "target=\"_blank\" href");
                CustomLayout csnormal = new CustomLayout(new ByteArrayInputStream(normalTooltip.getBytes()));
                csnormal.setWidth("250px");
                hzl.addComponent(csnormal);
                if (item.getWowId() != item.getWowId_hc() && item.getWowId_hc() != 0) {
                        xml = new XmlParser("" + item.getWowId_hc());
                        String hcTooltip = xml.parseXmlTooltip();
                        hcTooltip = hcTooltip.replace("href", "target=\"_blank\" href");
                        CustomLayout cshc = new CustomLayout(new ByteArrayInputStream(hcTooltip.getBytes()));
                        cshc.setWidth("250px");
                        hzl.addComponent(cshc);
                }
                addComponent(hzl);
        }

        private class UpdateButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {

                        updateItem();
                }
        }

        private class DeleteButtonClickListener implements ClickListener {

                private final Items item;

                private DeleteButtonClickListener(Items item) {
                        this.item = item;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        try {
                                deleteItem(item);
                                notifyListeners();
                                close();
                        } catch (SQLException ex) {
                                Logger.getLogger(ItemEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }

        private class ApplyButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        applyPrices();
                }
        }

        private class RequestButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        setPricesToDefault();
                }
        }
}
