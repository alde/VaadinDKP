/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.items.windows;

import com.unknown.entity.XmlParser;
import com.unknown.entity.items.ItemLooter;
import com.unknown.entity.items.Items;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class ItemInfoWindow extends Window {

        private final Items item;

        public ItemInfoWindow(Items item) {
                this.item = item;
                this.setCaption(item.getName());
                this.addStyleName("opaque");
                this.setPositionX(400);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() {
                try {
                        itemInformation();
                } catch (IOException ex) {
                        Logger.getLogger(ItemInfoWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                itemGrid();
                itemLootedByTable();

        }

        private GridLayout itemGridLayout(final Button wowIdBtn, final Button wowIdBtnhc) throws OverlapsException, OutOfBoundsException {
                GridLayout gl = new GridLayout(3, 3);
                gl.setWidth("300px");
                gl.addComponent(new Label("Normal "), 1, 0);
                gl.addComponent(new Label("Heroic "), 2, 0);
                gl.addComponent(new Label("Price: "), 0, 1);
                gl.addComponent(new Label("" + item.getPrice()), 1, 1);
                gl.addComponent(new Label("" + item.getPrice_hc()), 2, 1);
                gl.addComponent(new Label("Wowhead: "), 0, 2);
                Link normal = new Link("Normal", new ExternalResource("http://www.wowhead.com/item="+item.getWowId()));
                normal.setTargetName("_blank");
                Link heroic = new Link("Heroic", new ExternalResource("http://www.wowhead.com/item="+item.getWowId_hc()));
                heroic.setTargetName("_blank");
                gl.addComponent(normal, 1, 2);
                gl.addComponent(heroic, 2, 2);
                return gl;
        }

        private void itemLootedByTable() {
                addComponent(new Label("Looted by"));
                HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);
                Table lootedby = lootList(item);
                if (lootedby.size() > 0) {
                        lootedby.addStyleName("small striped");
                        hzl.addComponent(lootedby);
                } else {
                        hzl.addComponent(new Label("Not looted by anyone"));
                }
                addComponent(hzl);
        }

        private void itemGrid() throws OverlapsException, OutOfBoundsException {

                final Button wowIdBtn = new Button("" + item.getWowId());
                wowIdBtn.setStyleName(Button.STYLE_LINK);
                wowIdBtn.addListener(new WowIdButtonClickListener());
                final Button wowIdBtnhc = new Button("" + item.getWowId_hc());
                wowIdBtnhc.setStyleName(Button.STYLE_LINK);
                wowIdBtnhc.addListener(new WowIdHcButtonClickListener());

                GridLayout gl = itemGridLayout(wowIdBtn, wowIdBtnhc);
                addComponent(gl);
        }
        
        private void itemInformation() throws IOException {
                HorizontalLayout hzl = new HorizontalLayout();
                XmlParser xml = new XmlParser("" + item.getWowId());
                String normalTooltip = xml.parseXmlTooltip();
                normalTooltip = normalTooltip.replace("href", "target=\"_blank\" href");
                CustomLayout csnormal = new CustomLayout(new ByteArrayInputStream(normalTooltip.getBytes()));
                csnormal.setWidth("200px");
                hzl.addComponent(csnormal);
                if (item.getWowId() != item.getWowId_hc() && item.getWowId_hc() != 0) {
                        xml = new XmlParser("" + item.getWowId_hc());
                        String hcTooltip = xml.parseXmlTooltip();
                        hcTooltip=hcTooltip.replace("href", "target=\"_blank\" href");
                        CustomLayout cshc = new CustomLayout(new ByteArrayInputStream(hcTooltip.getBytes()));
                        cshc.setWidth("200px");
                        hzl.addComponent(cshc);
                }
                addComponent(hzl);
                addComponent(new Label("<hr>", Label.CONTENT_XHTML));
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
                addItem.getItemProperty("Name").setValue(looters.getName());
                addItem.getItemProperty("Price").setValue(looters.getPrice());
                addItem.getItemProperty("Raid").setValue(looters.getRaid());
                addItem.getItemProperty("Date").setValue(looters.getDate());
        }

        private void itemTableHeaders(Table tbl) throws UnsupportedOperationException {
                tbl.addContainerProperty("Name", String.class, "");
                tbl.addContainerProperty("Price", Double.class, 0);
                tbl.addContainerProperty("Raid", String.class, "");
                tbl.addContainerProperty("Date", String.class, "");
        }

        private class WowIdButtonClickListener implements ClickListener {

                public WowIdButtonClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        String url = "http://www.wowhead.com/item=" + item.getWowId();
                        getWindow().open(new ExternalResource(url), "_blank");
                }
        }

        private class WowIdHcButtonClickListener implements ClickListener {

                public WowIdHcButtonClickListener() {
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        String url = "http://www.wowhead.com/item=" + item.getWowId_hc();
                        getWindow().open(new ExternalResource(url), "_blank");
                }
        }
}
