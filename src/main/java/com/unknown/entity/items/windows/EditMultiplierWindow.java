package com.unknown.entity.items.windows;

import com.unknown.entity.character.CharacterInfoListener;
import com.unknown.entity.items.Multiplier;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.items.ItemInfoListener;
import com.unknown.entity.items.Items;
import com.unknown.entity.items.MultiplierList;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class EditMultiplierWindow extends Window {
        List<Multiplier> multipliers = new ArrayList<Multiplier>();
        TextField itemlevel;
        TextField multiplier;
        MultiplierList multiplierTable;
        private List<ItemInfoListener> listeners = new ArrayList<ItemInfoListener>();
        private List<CharacterInfoListener> charlisteners = new ArrayList<CharacterInfoListener>();

        public EditMultiplierWindow() {
                this.setCaption("Edit multipliers");
                this.addStyleName("opaque");
                this.center();
                this.setResizable(false);
                this.getContent().setSizeUndefined();
        }

        public void printInfo() {
                GridLayout gl = new GridLayout(3, 2);
                this.itemlevel = new TextField();
                this.multiplier = new TextField();
                Button addButton = new Button("Add");
                addButton.addListener(new AddButtonListener());
                Label ilvl = new Label("Itemlevel");
                Label mtpl = new Label("Multiplier");
                gl.addComponent(ilvl, 0, 0);
                gl.addComponent(itemlevel, 0, 1);
                gl.addComponent(mtpl, 1, 0);
                gl.addComponent(multiplier, 1, 1);
                gl.addComponent(addButton, 2, 1);
                addComponent(gl);

                this.multiplierTable = new MultiplierList();
                addComponent(multiplierTable);

                Button updateButton = new Button("Update");
                updateButton.addListener(new UpdateButtonListener());

                Button applyButton = new Button("Apply to looted");
                applyButton.addListener(new ApplyButtonListener());

                Button closeButton = new Button("Close");
                closeButton.addListener(new CloseButtonListener());


                HorizontalLayout hzl = new HorizontalLayout();
                hzl.addComponent(updateButton);
                hzl.addComponent(applyButton);
                hzl.addComponent(closeButton);
                hzl.setMargin(true);
                hzl.setSpacing(true);
                addComponent(hzl);
        }

        private void addItemlevel() {
                String mtp = multiplier.getValue().toString().replace(",", ".");
                ItemDB.addMultiplier(Integer.parseInt(itemlevel.getValue().toString()), Double.parseDouble(mtp.toString()));
                multiplierTable.update();
                itemlevel.setValue("");
                multiplier.setValue("");
        }

        public void addItemInfoListener(ItemInfoListener listener) {
                listeners.add(listener);
        }

        public void addCharacterInfoListener(CharacterInfoListener clistn) {
                charlisteners.add(clistn);
        }

        private void notifyListeners() {
                for (ItemInfoListener itemInfoListener : listeners) {
                        itemInfoListener.onItemInfoChange();
                }
                for (CharacterInfoListener charInfoListener : charlisteners) {
                        charInfoListener.onCharacterInfoChange();
                }
        }

        private class AddButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        addItemlevel();
                }
        }

        private class UpdateButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        multiplierTable.updateIlvls();
                        updateDefaultPricesOnAllItems();
                        notifyListeners();
                }
        }

        private class CloseButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        close();
                }
        }

        private void updateDefaultPricesOnAllItems() {
                List<Items> itemses = new ArrayList<Items>();
                itemses.addAll(ItemDB.getItems());
                for (Items i : itemses) {
                        Multiplier mp1 = ItemDB.getMultiplierForItemlevel(i.getIlvl());
                        BigDecimal formattedprice = new BigDecimal(ItemDB.getDefaultPrice(i) * mp1.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        Multiplier mp2 = ItemDB.getMultiplierForItemlevel(i.getIlvl() + 13);
                        BigDecimal formattedpricehc = new BigDecimal(ItemDB.getDefaultPrice(i) * mp2.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        ItemDB.updateItemPrices(i.getId(), formattedprice, formattedpricehc);
                }
        }

        private void updateLootedPrices() {
                List<Items> itemses = new ArrayList<Items>();
                itemses.addAll(ItemDB.getItems());
                for (Items i : itemses) {
                        Multiplier mp1 = ItemDB.getMultiplierForItemlevel(i.getIlvl());
                        BigDecimal formattedprice = new BigDecimal(ItemDB.getDefaultPrice(i) * mp1.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        Multiplier mp2 = ItemDB.getMultiplierForItemlevel(i.getIlvl() + 13);
                        BigDecimal formattedpricehc = new BigDecimal(ItemDB.getDefaultPrice(i) * mp2.getMultiplier()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                        ItemDB.updateLoots(i, formattedprice.toString(), formattedpricehc.toString());
                }
        }

        private class ApplyButtonListener implements ClickListener {

                @Override
                public void buttonClick(ClickEvent event) {
                        multiplierTable.updateIlvls();
                        updateLootedPrices();
                        notifyListeners();
                }
        }
}
