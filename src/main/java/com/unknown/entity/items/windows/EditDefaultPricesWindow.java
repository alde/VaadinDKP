/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.ItemPrices;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author alde
 */
public class EditDefaultPricesWindow extends Window {

        ItemDAO itemDao = null;
        List<ItemPrices> prices = new ArrayList<ItemPrices>();
        IndexedContainer ic;

        public EditDefaultPricesWindow() throws SQLException {
                this.setCaption("Edit default Prices");
                this.addStyleName("opaque");
                this.center();
                this.getContent().setSizeUndefined();
                this.itemDao = new ItemDB();
                this.prices.addAll(itemDao.getDefaultPrices());
                this.ic = new IndexedContainer();
        }

        public void printInfo() {
                Button updateButton = new Button("Update");
                updateButton.addListener(new UpdateButtonListener());
                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());
                Table priceTable = new Table();
                priceTable.setEditable(true);
                priceTable.setContainerDataSource(ic);
                priceTable.setImmediate(true);
                tableData(priceTable);
                addComponent(priceTable);
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(updateButton);
                hzl.addComponent(closeButton);
                hzl.setMargin(true);
                hzl.setSpacing(true);
                addComponent(hzl);
        }

        private void doUpdatePrices() {
                for (Iterator i = ic.getItemIds().iterator(); i.hasNext();) {
                        ItemPrices iid = (ItemPrices) i.next();
                        Item item = ic.getItem(iid);
                        itemDao.updateDefaultPrice(item.getItemProperty("Slot").toString(), Double.parseDouble((item.getItemProperty("Normal")).toString()), Double.parseDouble((item.getItemProperty("Heroic")).toString()));
                }
        }

        private void tableData(Table priceTable) {
                priceTable.addContainerProperty("Slot", String.class, "");
                priceTable.addContainerProperty("Normal", Double.class, 0);
                priceTable.addContainerProperty("Heroic", Double.class, 0);
                for (ItemPrices ip : prices) {
                        System.out.println(ip.getSlotString() + " price: " + ip.getPrice() + " heroic: " + ip.getPriceHeroic());
                        Item addItem = ic.addItem(ip);
                        priceTableSetRow(addItem, ip);
                }
        }

        private void priceTableSetRow(Item addItem, ItemPrices ip) throws ConversionException, ReadOnlyException {
                addItem.getItemProperty("Slot").setValue(ip.getSlotString());
                addItem.getItemProperty("Normal").setValue(ip.getPrice());
                addItem.getItemProperty("Heroic").setValue(ip.getPriceHeroic());
        }

        private class CloseButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private class UpdateButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        doUpdatePrices();
                }
        }
}
