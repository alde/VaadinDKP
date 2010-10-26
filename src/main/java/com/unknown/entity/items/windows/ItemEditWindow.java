/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.XmlParser;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.database.CharacterDB;
import com.unknown.entity.items.ItemInfoListener;
import com.unknown.entity.items.ItemLooter;
import com.unknown.entity.items.Items;
import com.vaadin.data.Item;
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
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

        private final Items item;
        private List<ItemInfoListener> listeners = new ArrayList<ItemInfoListener>();

        public ItemEditWindow(Items item) {
                this.item = item;
                this.addStyleName("opaque");
                this.setCaption("Edit item: " + item.getName());
                this.setPositionX(400);
                this.setPositionY(50);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() {
                addComponent(new Label("Item information"));
                final TextField name = editInfoName();
                final ComboBox slot = editInfoSlot();
                slot.setWidth("150px");
                slot.addStyleName("select-button");
                final ComboBox type = editInfoType();
                type.setWidth("150px");
                type.addStyleName("select-button");
                final TextField wowIdfield = editInfoWowIdField();
                final TextField price = editInfoPriceField();
                final TextField pricehc = editInfoPriceHcField();
                final TextField wowIdfieldhc = editInfoWowIdHcField();

                addComponent(name);
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(slot);
                hzl.addComponent(type);
                hzl.setSpacing(true);
                addComponent(hzl);

                itemEditGrid(wowIdfield, wowIdfieldhc, price, pricehc);

                Button updateButton = new Button("Update Item");
                Button deleteButton = new Button("Delete Item");
                deleteButton.addListener(new DeleteButtonClickListener(item));
                updateButton.addListener(new UpdateButtonClickListener(name, slot, type, wowIdfield, wowIdfieldhc, price, pricehc));
                hzl = new HorizontalLayout();
                Label warning = new Label();
                warning.setWidth("220px");
                warning.setValue("Can NOT be reverted.");
                warning.addStyleName("error");
                hzl.addComponent(updateButton);
                hzl.addComponent(deleteButton);
                hzl.addComponent(warning);
                hzl.setSpacing(true);
                hzl.setMargin(true, false, true, false);
                addComponent(hzl);
                itemLootedByTable();
                try {
                        itemTooltips(item);
                } catch (IOException ex) {
                        Logger.getLogger(ItemEditWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        private void itemEditGrid(final TextField wowIdfield, final TextField wowIdfieldhc, final TextField price, final TextField pricehc) throws OutOfBoundsException, OverlapsException {
                GridLayout gl = new GridLayout(3, 3);
                gl.setWidth("500px");

                Link normal = new Link("Normal", new ExternalResource("http://www.wowhead.com/item="+item.getWowId()));
                normal.setTargetName("_blank");
                Link heroic = new Link("Heroic", new ExternalResource("http://www.wowhead.com/item="+item.getWowId_hc()));
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

        private TextField editInfoPriceHcField() throws ReadOnlyException, ConversionException {
                final TextField pricehc = new TextField();
                pricehc.setImmediate(true);
                pricehc.setValue(item.getPrice_hc());
                return pricehc;
        }

        private TextField editInfoPriceField() throws ReadOnlyException, ConversionException {
                final TextField price = new TextField();
                price.setImmediate(true);
                price.setValue(item.getPrice());
                return price;
        }

        private TextField editInfoWowIdHcField() throws ConversionException, ReadOnlyException {
                final TextField wowIdfieldhc = new TextField();
                wowIdfieldhc.setImmediate(true);
                wowIdfieldhc.setValue("" + item.getWowId_hc());
                return wowIdfieldhc;
        }

        private TextField editInfoWowIdField() throws ReadOnlyException, ConversionException {
                final TextField wowIdfield = new TextField();
                wowIdfield.setImmediate(true);
                wowIdfield.setValue("" + item.getWowId());
                return wowIdfield;
        }

        private ComboBox editInfoType() throws ConversionException, ReadOnlyException, UnsupportedOperationException {
                final ComboBox type = new ComboBox("Type: ");
                for (Type types : Type.values()) {
                        type.addItem(types);
                }
                type.setNullSelectionAllowed(false);
                type.setValue(item.getType());
                type.setImmediate(true);
                return type;
        }

        private ComboBox editInfoSlot() throws UnsupportedOperationException, ConversionException, ReadOnlyException {
                final ComboBox slot = new ComboBox("Slot: ");
                for (Slots slots : Slots.values()) {
                        slot.addItem(slots);
                }
                slot.setNullSelectionAllowed(false);
                slot.setValue(Slots.valueOf(item.getSlot().replace("-", "")));
                slot.setImmediate(true);
                return slot;
        }

        private TextField editInfoName() {
                final TextField name = new TextField("Name: ", item.getName());
                name.setWidth("300px");
                name.setImmediate(true);
                return name;
        }

        private void itemLootedByTable() {
                addComponent(new Label("Looted by"));
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);
                Table lootedby = lootList(item);
                if (lootedby.size() > 0) {
                        lootedby.addStyleName("striped");
                        hzl.addComponent(lootedby);
                } else {
                        hzl.addComponent(new Label("Not looted by anyone"));
                }
                addComponent(hzl);
        }

        private int updateItem(String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc) {

                ItemDAO itemDao = new ItemDB();
                return itemDao.updateItem(item, newname, newslot, newtype, newwowid, newwowidhc, newprice, newpricehc);
        }

        private int deleteItem(Items item) throws SQLException {
                ItemDAO itemDao = new ItemDB();
                return itemDao.deleteItem(item.getId());
        }

        private Table lootList(Items item) {
                Table tbl = new Table();
                itemTableHeaders(tbl);
                tbl.setHeight(150);
                for (ItemLooter looters : item.getLooterList()) {
                        Item addItem = tbl.addItem(looters.getId());
                        itemTableRowAdd(addItem, looters);

                }
                return tbl;
        }

        private void itemTableRowAdd(Item addItem, ItemLooter looters) throws ConversionException, ReadOnlyException {
                Label looter = new Label(looters.getName());
                CharacterDAO charDao = new CharacterDB();
                looter.addStyleName(charDao.getRoleForCharacter(looters.getName()).toLowerCase().replace(" ", ""));
                addItem.getItemProperty("Name").setValue(looter);
                addItem.getItemProperty("Price").setValue(looters.getPrice());
                addItem.getItemProperty("Raid").setValue(looters.getRaid());
                addItem.getItemProperty("Date").setValue(looters.getDate());
        }

        private void itemTableHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", Label.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.addContainerProperty("Raid", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
        }

        public void addItemInfoListener(ItemInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (ItemInfoListener itemInfoListener : listeners) {
                        itemInfoListener.onItemInfoChange();
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

                private final TextField name;
                private final ComboBox slot;
                private final ComboBox type;
                private final TextField wowIdfield;
                private final TextField wowIdfieldhc;
                private final TextField price;
                private final TextField pricehc;

                public UpdateButtonClickListener(TextField name, ComboBox slot, ComboBox type, TextField wowIdfield, TextField wowIdfieldhc, TextField price, TextField pricehc) {
                        this.name = name;
                        this.slot = slot;
                        this.type = type;
                        this.wowIdfield = wowIdfield;
                        this.wowIdfieldhc = wowIdfieldhc;
                        this.price = price;
                        this.pricehc = pricehc;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        final String newname = name.getValue().toString();
                        final Slots newslot = (Slots) slot.getValue();
                        final Type newtype = (Type) type.getValue();
                        final int newwowid = Integer.parseInt(wowIdfield.getValue().toString());
                        final int newwowidhc = Integer.parseInt(wowIdfieldhc.getValue().toString());
                        final double newprice = Double.parseDouble(price.getValue().toString());
                        final double newpricehc = Double.parseDouble(pricehc.getValue().toString());
                        final int success = updateItem(newname, newslot, newtype, newwowid, newwowidhc, newprice, newpricehc);
                        addComponent(new Label("Success: " + success));
                        notifyListeners();
                        close();
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
}
