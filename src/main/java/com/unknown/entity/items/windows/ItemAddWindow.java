/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.database.ItemDB;
import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.XmlParser;
import com.unknown.entity.items.*;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alde
 */
public class ItemAddWindow extends Window {

        private List<ItemInfoListener> listeners = new ArrayList<ItemInfoListener>();
        private TextField name;
        private TextField wowid;
        private TextField wowidheroic;
        private TextField price;
        private TextField priceheroic;
        private ComboBox slot;
        private ComboBox type;
        private VerticalLayout vrt;
        private TextField itemlevel;
        private TextField quality;
        private List<ItemPrices> prices;

        public ItemAddWindow() {
                this.setCaption("Add Item");
                this.setPositionX(400);
                this.setPositionY(100);
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
        }

        public void printInfo() {

                DefaultPrices def = new DefaultPrices();
                this.prices = new ArrayList<ItemPrices>();

                prices = def.getPrices();

                VerticalLayout addItem = new VerticalLayout();
                vrt = new VerticalLayout();
                addComponent(addItem);
                itemSetters();
                itemAddWIndowAddComponents(addItem);

                for (Slots slots : Slots.values()) {
                        this.slot.addItem(slots);
                }
                for (Type types : Type.values()) {
                        this.type.addItem(types);
                }

                slot.addListener(new SlotComboBoxValueChangeListener());

                final Button btn = new Button("Add");
                btn.addListener(new AddButtonClickListener());
                final Button cbtn = new Button("Close");
                cbtn.addListener(new CloseButtonClickListener());
                final Button clearButton = new Button("Clear");
                clearButton.addListener(new ClearButtonClickListener());
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);
                hzl.addComponent(btn);
                hzl.addComponent(cbtn);
                hzl.addComponent(clearButton);
                addItem.addComponent(hzl);
        }

        private void itemSetters() {
                this.name = new TextField("Name");
                this.name.setStyleName("textfieldfont");
                this.name.setWidth("150px");
                this.wowid = new TextField("WowID Normal");
                this.wowid.setStyleName("textfieldfont");
                this.wowid.setWidth("150px");
                this.wowidheroic = new TextField("WowID Heroic");
                this.wowidheroic.setStyleName("textfieldfont");
                this.wowidheroic.setWidth("150px");
                this.price = new TextField("Price Normal");
                this.price.setStyleName("textfieldfont");
                this.price.setWidth("150px");
                this.priceheroic = new TextField("Price Heroic");
                this.priceheroic.setStyleName("textfieldfont");
                this.priceheroic.setWidth("150px");

                this.slot = new ComboBox("Slot");
                this.slot.setStyleName("textfieldfont");
                this.slot.setWidth("150px");

                this.type = new ComboBox("Type");
                this.type.setStyleName("textfieldfont");
                this.type.setWidth("150px");

                this.itemlevel = new TextField("Base Itemlevel");
                this.itemlevel.setStyleName("textfieldfont");
                this.itemlevel.setWidth("150px");

                this.quality = new TextField("Quality");
                this.quality.setStyleName("textfieldfont");
                this.quality.setWidth("150px");

                this.name.setImmediate(true);
                this.name.focus();
                this.wowid.setImmediate(true);
                this.wowidheroic.setImmediate(true);
                this.price.setImmediate(true);
                this.priceheroic.setImmediate(true);
                this.slot.setImmediate(true);
                this.type.setImmediate(true);
                this.itemlevel.setImmediate(true);
                this.quality.setImmediate(true);
        }

        private void itemAddWIndowAddComponents(VerticalLayout addItem) {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(name);
                Button queryButton = new Button();
                queryButton.setIcon(new ThemeResource("../shared/query.png"));
                queryButton.setStyle(Button.STYLE_LINK);
                queryButton.addListener(new QueryListener());
                hzl.addComponent(queryButton);
                addItem.addComponent(hzl);
                addItem.addComponent(wowid);
                addItem.addComponent(wowidheroic);
                addItem.addComponent(slot);
                addItem.addComponent(type);
                addItem.addComponent(price);
                addItem.addComponent(priceheroic);
                addItem.addComponent(itemlevel);
                addItem.addComponent(quality);
        }

        private int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type, int ilvl, String qual) {
                return ItemDB.addItem(name, wowid, wowid_hc, price, price_hc, slot, type, ilvl, qual);
        }

        public void addItemInfoListener(ItemInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (ItemInfoListener itemInfoListener : listeners) {
                        itemInfoListener.onItemInfoChange();
                }
        }

        private class SlotComboBoxValueChangeListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        slotChanged();
                }
        }

        private void slotChanged() {
                int ilvl = Integer.parseInt(itemlevel.getValue().toString());
                String slotvalue = slot.getValue().toString();
                double defprice = 0.0;
                double defpricehc = 0.0;
                BigDecimal formattedprice = new BigDecimal(0), formattedpricehc = new BigDecimal(0);
                for (ItemPrices ip : prices) {
                        if (ip.getSlotString().equals(slotvalue)) {
                                Multiplier mp = ItemDB.getMultiplierForItemlevel(ilvl);
                                defprice = ip.getPrice() * mp.getMultiplier();
                                formattedprice = new BigDecimal(defprice).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                                mp = ItemDB.getMultiplierForItemlevel(ilvl + 13);
                                defpricehc = ip.getPrice() * mp.getMultiplier();
                                formattedpricehc = new BigDecimal(defpricehc).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        }
                }
                price.setValue("" + formattedprice);
                priceheroic.setValue("" + formattedpricehc);
        }

        private class AddButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addItem();
                }
        }

        private void addItem() {
                final String iname = (String) name.getValue();
                if (wowid.getValue().toString().isEmpty()) {
                        wowid.setValue("0");
                }
                if (wowidheroic.getValue().toString().isEmpty()) {
                        wowidheroic.setValue("0");
                }
                if (price.getValue().toString().isEmpty()) {
                        price.setValue("0");
                }
                if (priceheroic.getValue().toString().isEmpty()) {
                        priceheroic.setValue("0");
                }
                final int iwowid = Integer.parseInt(wowid.getValue().toString());
                final int iwowidheroic = Integer.parseInt(wowidheroic.getValue().toString());
                final double iprice = Double.parseDouble(price.getValue().toString());
                final double ipriceheroic = Double.parseDouble(priceheroic.getValue().toString());
                final String islot = slot.getValue().toString();
                final String itype = type.getValue().toString();
                final int iilvl = Integer.parseInt(itemlevel.getValue().toString());
                final String iquality = quality.getValue().toString();
                int success = 0;

                success = addItem(iname, iwowid, iwowidheroic, iprice, ipriceheroic, islot, itype, iilvl, iquality);
                if (success < 0) {
                        vrt.addComponent(new Label("Item already in database\n" + iname));
                } else {
                        notifyListeners();
                        close();
                }
        }

        private void queryData() throws ConversionException, ReadOnlyException {
                String query = "";
                if (!name.getValue().toString().isEmpty()) {
                        query = name.getValue().toString();
                } else if (!wowid.getValue().toString().isEmpty()) {
                        query = wowid.getValue().toString();
                } else if (!wowidheroic.getValue().toString().isEmpty()) {
                        query = wowidheroic.getValue().toString();
                }
                if (!query.isEmpty()) {
                        query = query.replace(" ", "%20");
                        XmlParser xml = new XmlParser(query);
                        String typeFromXml = xml.parseXmlType();
                        vrt.removeAllComponents();
                        addComponent(vrt);
                        if (!typeFromXml.equalsIgnoreCase("Item not found!")) {
                                String tooltip = xml.parseXmlTooltip();
                                String wowidFromXml = xml.parseXmlWowid();
                                String ilevel = xml.parseXmlItemLevel();
                                if (tooltip.contains("Heroic")) {
                                        int ilvl = Integer.parseInt(ilevel) - 13;
                                        wowidheroic.setValue(wowidFromXml);
                                        wowid.setValue("");
                                        itemlevel.setValue("" + ilvl);
                                } else {
                                        wowid.setValue(wowidFromXml);
                                        wowidheroic.setValue("");
                                        itemlevel.setValue(ilevel);
                                }
                                type.setValue(Type.valueOf(typeFromXml));
                                String slotsFromXml = xml.parseXmlSlots();
                                slot.setValue(Slots.valueOf(slotsFromXml));
                                String itemname = xml.parseXmlName();
                                name.setValue(itemname);
                                String itemquality = xml.parseXmlQuality();
                                quality.setValue(itemquality);
                                String url = xml.parseXmlUrl();
                                Link seealso = new Link(itemname, new ExternalResource(url + "#see-also"));
                                seealso.setTargetName("_blank");
                                vrt.addComponent(new Label("See also for non-queryable wow-id:"));
                                vrt.addComponent(seealso);
                        } else {
                                vrt.addComponent(new Label("Item not found!"));
                        }
                } else {
                        vrt.addComponent(new Label("Gotta have an item to query."));
                }
        }

        private class CloseButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class QueryListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        queryData();
                }
        }

        private class ClearButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        clearAll();
                }
        }

        private void clearAll() {
                name.setValue("");
                wowid.setValue("");
                wowidheroic.setValue("");
                price.setValue("");
                priceheroic.setValue("");
                slot.setValue("Other");
                type.setValue("Other");
                itemlevel.setValue("");
                quality.setValue("");
                vrt.removeAllComponents();
        }
}
