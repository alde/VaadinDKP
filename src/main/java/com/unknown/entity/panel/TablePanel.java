/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.unknown.entity.Armor;
import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.character.CharacterList;
import com.unknown.entity.character.DkpList;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.raids.RaidList;
import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import org.vaadin.henrik.superimmediatetextfield.SuperImmediateTextField;

/**
 *
 * @author alde
 */
public class TablePanel {

        private CharacterList charList;
        private DkpList dkpList;
        private ItemList itemList;
        private RaidList raidList;

        public TablePanel(DkpList dkpList, ItemList itemList, RaidList raidList) {
                this.dkpList = dkpList;
                this.itemList = itemList;
                this.raidList = raidList;
        }

        public HorizontalLayout HorizontalSegment() {
                final HorizontalLayout hzl = new HorizontalLayout();
                hzl.setSpacing(true);

                // Vertical DKP List
                VerticalLayout vertDkp = VerticalDKPListLayout();
                hzl.addComponent(vertDkp);
                
                // Vertical Item List
                VerticalLayout vertItem = VerticalItemListLayout();
                hzl.addComponent(vertItem);

                // Vertical Raid List
                VerticalLayout vertRaid = VerticalRaidListLayout();
                hzl.addComponent(vertRaid);

                return hzl;
        }

        private VerticalLayout VerticalRaidListLayout() {
                VerticalLayout vertRaid = new VerticalLayout();
                vertRaid.addComponent(new Label("Raids"));
                vertRaid.addComponent(raidList);
                raidList.printList();
                return vertRaid;
        }

        private VerticalLayout VerticalItemListLayout() {
                VerticalLayout vertItem = new VerticalLayout();
                vertItem.addComponent(new Label("Items"));
                vertItem.addComponent(itemList);
                HorizontalLayout hzl = new HorizontalLayout();
                SuperImmediateTextField itemname = itemNameFilterField();
                itemname.setStyleName("textfieldfont");
                ComboBox itemslot = itemSlotFilterBox();
                ComboBox itemtype = itemTypeFilterBox();
                hzl.addComponent(itemname);
                hzl.addComponent(itemslot);
                hzl.addComponent(itemtype);
                hzl.setSpacing(true);
                vertItem.addComponent(hzl);
                itemList.printList();
                return vertItem;
        }

        private SuperImmediateTextField itemNameFilterField() {
                SuperImmediateTextField itemname = new SuperImmediateTextField("Filter itemname");
                itemname.setImmediate(true);
                itemname.addListener(new ItemNameFieldValueChangeListener(itemList, itemname));
                return itemname;
        }

        public CharacterList getCharList() {
                return charList;
        }

        public ItemList getItemList() {
                return itemList;
        }

        public RaidList getRaidList() {
                return raidList;
        }

        public DkpList getDkpList() {
                return dkpList;
        }

        private ComboBox itemSlotFilterBox() throws ReadOnlyException, ConversionException, UnsupportedOperationException {
                ComboBox itemslot = new ComboBox("Filter itemslot");
                itemslot.addStyleName("select-button");
                itemslot.setWidth("180px");
                itemslot.addItem("<none>");
                for (Slots slot : Slots.values()) {
                        itemslot.addItem(slot);
                }
                itemslot.setNullSelectionAllowed(false);
                Collection<?> itemIds = itemslot.getItemIds();
                itemslot.setValue(itemIds.iterator().next());
                itemslot.setImmediate(true);
                itemslot.addListener(new ItemSlotFilterBoxChangeListener(itemList, itemslot));
                return itemslot;
        }

        private ComboBox itemTypeFilterBox() throws UnsupportedOperationException, ReadOnlyException, ConversionException {
                ComboBox itemtype = new ComboBox("Filter itemtype");
                itemtype.addStyleName("select-button");
                itemtype.setWidth("180px");
                itemtype.addItem("<none>");
                for (Type type : Type.values()) {
                        itemtype.addItem(type);
                }
                itemtype.setNullSelectionAllowed(false);
                Collection<?> itemIds = itemtype.getItemIds();
                itemtype.setValue(itemIds.iterator().next());
                itemtype.setImmediate(true);
                itemtype.addListener(new ItemTypeFilterBoxChangeListener(itemList, itemtype));
                return itemtype;
        }

        private VerticalLayout VerticalDKPListLayout() throws UnsupportedOperationException {
                VerticalLayout vertDkp = new VerticalLayout();
                vertDkp.addComponent(new Label("DKP"));
                vertDkp.addComponent(dkpList);
                dkpFilterBox(vertDkp);

                dkpList.printList();
                return vertDkp;
        }

        private void dkpFilterBox(VerticalLayout vertDkp) throws UnsupportedOperationException, ReadOnlyException, ConversionException {
                final ComboBox filterDkp = new ComboBox("Filter");
                filterDkp.addStyleName("select-button");
                filterDkp.setWidth("180px");
                filterDkp.setImmediate(true);
                vertDkp.addComponent(filterDkp);
                filterDkp.addItem("<none>");
                for (Armor armor : Armor.values()) {
                        filterDkp.addItem(armor);
                }
                filterDkp.setNullSelectionAllowed(false);
                Collection<?> itemIds = filterDkp.getItemIds();
                filterDkp.setValue(itemIds.iterator().next());
                filterDkp.addListener(new DkpFilterChangeListener(dkpList, filterDkp));
        }

        private class DkpFilterChangeListener implements ValueChangeListener {

                private final DkpList dkpList;
                private final ComboBox filterDkp;

                public DkpFilterChangeListener(DkpList dkpList, ComboBox filterDkp) {
                        this.dkpList = dkpList;
                        this.filterDkp = filterDkp;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (filterDkp.getValue().equals("<none>")) {
                                dkpList.filter(null);
                        } else {
                                dkpList.filter(filterDkp.getValue());
                        }
                }
        }

        private class ItemTypeFilterBoxChangeListener implements ValueChangeListener {

                private final ItemList itemList;
                private final ComboBox itemTypeFilterBox;

                public ItemTypeFilterBoxChangeListener(ItemList itemList, ComboBox itemTypeFilterBox) {
                        this.itemList = itemList;
                        this.itemTypeFilterBox = itemTypeFilterBox;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (itemTypeFilterBox.getValue().equals("<none>")) {
                                itemList.filterType(null);
                        } else {
                                itemList.filterType(itemTypeFilterBox.getValue());
                        }
                }
        }

        private class ItemSlotFilterBoxChangeListener implements ValueChangeListener {

                private final ItemList itemList;
                private final ComboBox itemSlotFilterBox;

                private ItemSlotFilterBoxChangeListener(ItemList itemList, ComboBox itemSlotFilterBox) {
                        this.itemList = itemList;
                        this.itemSlotFilterBox = itemSlotFilterBox;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {
                        if (itemSlotFilterBox.getValue().equals("<none>")) {
                                itemList.filterSlot(null);
                        } else {
                                itemList.filterSlot(itemSlotFilterBox.getValue());
                        }
                }
        }

        private class ItemNameFieldValueChangeListener implements ValueChangeListener {

                private final ItemList itemList;
                private final TextField itemNameField;

                private ItemNameFieldValueChangeListener(ItemList itemList, TextField itemNameField) {
                        this.itemList = itemList;
                        this.itemNameField = itemNameField;
                }

                @Override
                public void valueChange(ValueChangeEvent event) {

                        itemList.filterName(itemNameField.getValue());

                }
        }
}
