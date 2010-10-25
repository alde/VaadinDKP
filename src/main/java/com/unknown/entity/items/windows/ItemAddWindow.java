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
import com.unknown.entity.items.*;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        public ItemAddWindow() {
                this.setCaption("Add Item");
                this.setPositionX(400);
                this.setPositionY(100);
                this.addStyleName("opaque");
                this.getContent().setSizeUndefined();
        }

        public void printInfo() {

                DefaultPrices def = new DefaultPrices(new ItemDB());
                List<ItemPrices> prices = new ArrayList<ItemPrices>();
                try {
                        prices = def.getPrices();
                } catch (SQLException ex) {
                        Logger.getLogger(ItemAddWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

                VerticalLayout addItem = new VerticalLayout();
                vrt = new VerticalLayout();
                addComponent(addItem);
                itemSetters();

                final List<ItemPrices> defaultprices = prices;

                this.name.setImmediate(true);
                this.name.focus();
                this.wowid.setImmediate(true);
                this.wowidheroic.setImmediate(true);
                this.price.setImmediate(true);
                this.priceheroic.setImmediate(true);
                this.slot.setImmediate(true);
                this.type.setImmediate(true);

                itemAddWIndowAddComponents(addItem);

                for (Slots slots : Slots.values()) {
                        this.slot.addItem(slots);
                }
                for (Type types : Type.values()) {
                        this.type.addItem(types);
                }

                slot.addListener(new SlotComboBoxValueChangeListener(slot, defaultprices, price, priceheroic));

                final Button btn = new Button("Add");
                btn.addListener(new AddButtonClickListener(name, wowid, wowidheroic, price, priceheroic, slot, type, vrt));
                final Button cbtn = new Button("Close");
                cbtn.addListener(new CloseButtonClickListener());
                final Button clearButton = new Button("Clear");
                clearButton.addListener(new ClearButtonClickListener(name, wowid, wowidheroic, price, priceheroic, slot, type, vrt));
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
                this.slot.addStyleName("select-button");
                this.type = new ComboBox("Type");
                this.type.setStyleName("textfieldfont");
                this.type.setWidth("150px");
                this.type.addStyleName("select-button");
        }

        private void itemAddWIndowAddComponents(VerticalLayout addItem) {
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(name);
                Button queryButton = new Button();
                queryButton.setIcon(new ThemeResource("../shared/query.png"));
                queryButton.setStyle(Button.STYLE_LINK);
                queryButton.addListener(new QueryListener(name, vrt));
                hzl.addComponent(queryButton);
                addItem.addComponent(hzl);
                addItem.addComponent(wowid);
                addItem.addComponent(wowidheroic);
                addItem.addComponent(slot);
                addItem.addComponent(type);
                addItem.addComponent(price);
                addItem.addComponent(priceheroic);
        }

        private int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type) throws SQLException {
                ItemDAO itemDao = new ItemDB();
                return itemDao.addItem(name, wowid, wowid_hc, price, price_hc, slot, type);
        }

        public void addItemInfoListener(ItemInfoListener listener) {
                listeners.add(listener);
        }

        private void notifyListeners() {
                for (ItemInfoListener itemInfoListener : listeners) {
                        itemInfoListener.onItemInfoChange();
                }
        }

        private static class SlotComboBoxValueChangeListener implements ValueChangeListener {

                private final ComboBox slot;
                private final List<ItemPrices> defaultprices;
                private final TextField price;
                private final TextField priceheroic;

                public SlotComboBoxValueChangeListener(ComboBox slot, List<ItemPrices> defaultprices, TextField price, TextField priceheroic) {
                        this.slot = slot;
                        this.defaultprices = defaultprices;
                        this.price = price;
                        this.priceheroic = priceheroic;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        String slotvalue = slot.getValue().toString();
                        double defprice = 0.0;
                        double defpricehc = 0.0;
                        for (ItemPrices ip : defaultprices) {
                                if (ip.getSlotString().equals(slotvalue)) {
                                        defprice = ip.getPrice();
                                        defpricehc = ip.getPriceHeroic();
                                }
                        }
                        price.setValue("" + defprice);
                        priceheroic.setValue("" + defpricehc);
                }
        }

        private class AddButtonClickListener implements ClickListener {

                private final TextField name;
                private final TextField wowid;
                private final TextField wowidheroic;
                private final TextField price;
                private final TextField priceheroic;
                private final ComboBox slot;
                private final ComboBox type;
                private final VerticalLayout vert;

                public AddButtonClickListener(TextField name, TextField wowid, TextField wowidheroic, TextField price, TextField priceheroic, ComboBox slot, ComboBox type, VerticalLayout vert) {
                        this.name = name;
                        this.wowid = wowid;
                        this.wowidheroic = wowidheroic;
                        this.price = price;
                        this.priceheroic = priceheroic;
                        this.slot = slot;
                        this.type = type;
                        this.vert = vert;
                }

                @Override
                public void buttonClick(ClickEvent event) {
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
                        int success = 0;
                        try {
                                success = addItem(iname, iwowid, iwowidheroic, iprice, ipriceheroic, islot, itype);

                                if (success < 0) {
                                        vert.addComponent(new Label("Item already in database\n"+iname));
                                } else {
                                        notifyListeners();
                                        close();
                                }
                        } catch (SQLException ex) {
                                Logger.getLogger(ItemAddWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }

                }
        }

        private class CloseButtonClickListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class QueryListener implements ClickListener {

                private TextField name;
                private VerticalLayout vrt;

                public QueryListener(TextField name, VerticalLayout vrt) {
                        this.name = name;
                        this.vrt = vrt;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        String query = name.getValue().toString();
                        query = query.replace(" ", "%20");
                        XmlParser xml = new XmlParser(query);
                        String typeFromXml = xml.parseXmlType();
                        vrt.removeAllComponents();
                        addComponent(vrt);
                        if (!typeFromXml.equalsIgnoreCase("Item not found!")) {
                                String tooltip = xml.parseXmlTooltip();
                                String wowidFromXml = xml.parseXmlWowid();
                                if (tooltip.contains(("Heroic"))) {
                                        wowidheroic.setValue(wowidFromXml);
                                } else {
                                        wowid.setValue(wowidFromXml);
                                }
                                type.setValue(Type.valueOf(typeFromXml));
                                String slotsFromXml = xml.parseXmlSlots();
                                slot.setValue(Slots.valueOf(slotsFromXml));

                                String itemname = xml.parseXmlName();
                                name.setValue(itemname);
                                String url = xml.parseXmlUrl();
                                Link seealso = new Link(itemname, new ExternalResource(url + "#see-also"));
                                seealso.setTargetName("_blank");
                                vrt.addComponent(new Label("See also for non-queryable wow-id:"));
                                vrt.addComponent(seealso);
                        } else {
                                vrt.addComponent(new Label("Item not found!"));
                        }
                }
        }

        private static class ClearButtonClickListener implements ClickListener {

                private final TextField name;
                private final TextField wowid;
                private final TextField wowidheroic;
                private final TextField price;
                private final TextField priceheroic;
                private final ComboBox slot;
                private final ComboBox type;
                private final VerticalLayout vert;

                public ClearButtonClickListener(TextField name, TextField wowid, TextField wowidheroic, TextField price, TextField priceheroic, ComboBox slot, ComboBox type, VerticalLayout vert) {
                        this.name = name;
                        this.wowid = wowid;
                        this.wowidheroic = wowidheroic;
                        this.price = price;
                        this.priceheroic = priceheroic;
                        this.slot = slot;
                        this.type = type;
                        this.vert = vert;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        name.setValue("");
                        wowid.setValue("");
                        wowidheroic.setValue("");
                        price.setValue("");
                        priceheroic.setValue("");
                        slot.setValue("Other");
                        type.setValue("Other");
                        vert.removeAllComponents();
                }
        }
}
