package com.unknown.entity.raids;

import com.unknown.entity.PopUpControl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;

/**
 *
 * @author alde
 */
public class RaidLootList extends Table implements RaidLootListener {

        IndexedContainer ic;
        private final RaidLootList raidLootList = this;
        private Raid raid;

        public RaidLootList(Raid raid) {
                this.ic = new IndexedContainer();
                this.raid = raid;
                this.setSelectable(true);
                this.setHeight("500px");
                this.setWidth("450px");
                this.addListener(new RewardListClickListener());
                raidRewardListSetHeaders();
                printList();
        }

        private void update() {
                ic.removeAllItems();
                ic.removeAllContainerFilters();
                printList();
        }

        @Override
        public void onRaidInfoChanged() {
                update();
        }

        private void raidRewardListSetHeaders() {
                ic.addContainerProperty("Name", String.class, "");
                ic.addContainerProperty("Item", String.class, "");
                ic.addContainerProperty("Price", Double.class, 0);
                ic.addContainerProperty("Heroic", String.class, "");
                this.setContainerDataSource(ic);
        }

        public void clear() {
                this.removeAllItems();
        }

        private void printList() {
                for (RaidItem item : raid.getRaidItems()) {
                        Item addItem = ic.addItem(item);
                        raidListAddRow(addItem, item);
                }
        }

        private void raidListAddRow(Item addItem, RaidItem item) {
                addItem.getItemProperty("Name").setValue(item.getLooter());
                addItem.getItemProperty("Item").setValue(item.getName());
                addItem.getItemProperty("Price").setValue(item.getPrice());
                addItem.getItemProperty("Heroic").setValue(item.isHeroic());
        }

        private class RewardListClickListener implements ItemClickListener {

                @Override
                public void itemClick(ItemClickEvent event) {
                        if (event.isDoubleClick()) {
                                RaidItem ritem = (RaidItem) event.getItemId();
                                PopUpControl pop = new PopUpControl(RaidLootList.this.getApplication());
                                pop.setRaidLootList(raidLootList);
                                pop.showProperRaidLootWindow(raid, ritem);
                        }
                }
        }
}
