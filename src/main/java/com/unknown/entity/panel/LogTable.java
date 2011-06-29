
package com.unknown.entity.panel;

import com.unknown.entity.Log;
import com.unknown.entity.Logg;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;


class LogTable extends Table {

        private IndexedContainer ic;

        public LogTable(String caption) {
                this.ic = new IndexedContainer();
                this.setCaption(caption);
                this.setWidth("860px");
                this.addStyleName("striped");
                setTableHeaders();
                Logg logs = new Logg();
                for (Log l : logs.readLog()) {
                        Item addItem = ic.addItem(l);
                        addTableRow(addItem, l);
                }
                Object [] properties={"Date"};
                boolean [] ordering={false,true};
                ic.sort(properties, ordering);
        }

        private void setTableHeaders() {
                ic.addContainerProperty("Date", String.class, "");
                ic.addContainerProperty("User", String.class, "");
                ic.addContainerProperty("Message", String.class, "");
                ic.addContainerProperty("Type", String.class, "");
                this.setContainerDataSource(ic);
        }

        private void addTableRow(Item addItem, Log l) {
                addItem.getItemProperty("Date").setValue(l.getDate());
                addItem.getItemProperty("User").setValue(l.getUsername());
                addItem.getItemProperty("Message").setValue(l.getMessage());
                addItem.getItemProperty("Type").setValue(l.getType());
        }

        void filter(String filter) {
                if (filter.equalsIgnoreCase("all"))
                        filter = "";
                ic.removeContainerFilters("Type");
                if (!filter.isEmpty())
                        ic.addContainerFilter("Type", filter, true, false);
                this.requestRepaint();
        }
}
