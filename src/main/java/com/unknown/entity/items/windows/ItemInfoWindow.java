
package com.unknown.entity.items.windows;

import com.unknown.entity.XmlParser;
import com.unknown.entity.items.ItemLooterTable;
import com.unknown.entity.items.Items;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ItemInfoWindow extends Window {


        private final Items item;

        private ItemLooterTable ilt;

        public ItemInfoWindow(Items item) {
                this.item = item;
                this.setCaption(item.getName());
                this.addStyleName("opaque");
                this.setPositionX(400);
                this.setPositionY(100);
                this.getContent().setSizeUndefined();
                this.ilt = new ItemLooterTable(item);
        }

        public void printInfo() {
                try {
                        itemInformation();
                } catch (IOException ex) {
                        Logger.getLogger(ItemInfoWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                itemGrid();
                addComponent(new Label("Looted by: "));
                addComponent(ilt);
        }

        private void itemGrid() throws OverlapsException, OutOfBoundsException {
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
}
