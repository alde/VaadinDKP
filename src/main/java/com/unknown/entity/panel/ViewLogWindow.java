/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.panel;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

/**
 *
 * @author alde
 */
class ViewLogWindow extends Window {

        private LogTable logs;
        private final ComboBox filterBox;

        public ViewLogWindow() {
                this.setWidth("900px");
                this.setPositionX(100);
                this.setPositionY(50);
                this.setCaption("Logs");
                this.filterBox = new ComboBox("Filter");
                this.filterBox.setWidth("200px");
        }

        public void printInfo() {
                filterBox.addItem("all");
                filterBox.addItem("raid");
                filterBox.addItem("item");
                filterBox.addItem("char");
                filterBox.setValue("all");
                filterBox.setImmediate(true);
                filterBox.setNullSelectionAllowed(false);
                filterBox.addListener(new FilterListener());
                addComponent(filterBox);
                this.logs = new LogTable("Logs");
                
                addComponent(logs);
        }

        private void filter(String s) {
                logs.filter(s);
        }

        private class FilterListener implements ValueChangeListener {

                @Override
                public void valueChange(ValueChangeEvent event) {
                        filter(filterBox.getValue().toString());
                }
        }
}
