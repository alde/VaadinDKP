/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.database.RaidDB;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import java.util.Collections;
import java.util.Comparator;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author alde
 */
public class AdjustmentTable extends Table {
        private IndexedContainer ic = new IndexedContainer();
        private User user;

        public AdjustmentTable(User user) {
                this.user = user;
                printList();
        }

        private void printList() {
                this.ic = new IndexedContainer();
                adjustmentTableSetHeaders();
                setHeight("200px");
                setContainerDataSource(ic);
                List<Adjustment> puns = RaidDB.getAdjustmentsForCharacter(user.getId());
                Collections.sort(puns, new ComparePunishmentDates());
                for (Adjustment cPun : puns) {
                        Item addItem = addItem(cPun);
                        adjustmentTableAddRow(addItem, cPun);
                }
                if (this.getWidth() < 300) {
                        this.setWidth("300px");
                }
        }

        public void update() {
                this.removeAllItems();
                this.requestRepaintRequests();
                printList();
                this.requestRepaint();
        }

        private void adjustmentTableAddRow(Item addItem, Adjustment pun) throws ConversionException, ReadOnlyException {
                addItem.getItemProperty("Comment").setValue(pun.getComment());
                addItem.getItemProperty("Shares").setValue(pun.getShares());
                addItem.getItemProperty("Date").setValue(pun.getDate());
        }

        private void adjustmentTableSetHeaders() throws UnsupportedOperationException {
                ic.addContainerProperty("Comment", String.class, "");
                ic.addContainerProperty("Shares", Integer.class, 0);
                ic.addContainerProperty("Date", String.class, "");
        }

          private class ComparePunishmentDates implements Comparator<Adjustment> {

                @Override
                public int compare(Adjustment t, Adjustment t1) {
                        return t1.getDate().compareTo(t.getDate());
                }
        }
}
