/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author alde
 */
public class XmlParser {

        private String startString = "http://cata.wowhead.com/item=";
        private String endString = "&xml";
        private String query;
        private Element root;

        public XmlParser(String query) {
                this.query = query;
                try {
                        rootXmlElement(startString + query + endString);
                } catch (JDOMException ex) {
                        Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        public boolean isEmpty() {
                return isEmpty();
        }

        public String parseXmlTooltip() {

                String output = "";
                if (!query.isEmpty()) {
                        if (!isErrorXml(root).equalsIgnoreCase("Item not found!")) {
                                output = findElement(root, "htmlTooltip");
                        } else {
                                output = "Item not found!";
                        }
                }
                return output;
        }

        private void rootXmlElement(String idString) throws JDOMException, IOException {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(idString);
                root = doc.getRootElement();
        }

        public String findElement(Element root, String targetElement) {
                return root.getChild("item").getChild(targetElement).getText();

        }

        private String isErrorXml(Element root) {
                if (root != null) {
                        String error = root.getChildText("error");
                        if (error != null) {
                                return error;
                        } else {
                                return "";
                        }
                } else {
                        return "";
                }
        }

        public String parseXmlWowid() {
                String wowid = findElement(root, "link");
                wowid = wowid.replace("http://cata.wowhead.com/item=", "");
                return wowid;
        }

        public String parseXmlType() {
                String foo = "";
                if (!query.isEmpty()) {
                        if (!isErrorXml(root).equalsIgnoreCase("Item not found!")) {
                                foo = findElement(root, "class");
                                if (foo.equals("Armor")) {
                                        foo = findElement(root, "subclass");
                                        if (foo.contains("Armor")) {
                                                foo = foo.replace(" Armor", "");
                                        } else if (foo.contains("Shields")) {
                                                foo = "Shields";
                                        } else {
                                                foo = "Other";
                                        }
                                } else if (foo.equals("Miscellaneous")) {
                                        if (findElement(root, "subclass").equalsIgnoreCase("Armor Tokens")) {
                                                foo = findElement(root, "name");
                                                if (foo.contains("Vanquisher")) {
                                                        foo = "vanquisher";
                                                } else if (foo.contains("Conqueror")) {
                                                        foo = "conqueror";
                                                } else if (foo.contains("Protector")) {
                                                        foo = "protector";
                                                } else {
                                                        foo = "Other";
                                                }
                                        }
                                } else if (foo.equals("Quest")) {
                                        foo = "Other";
                                } else if (foo.equals("Consumables"))
                                        foo = "Other";
                        } else {
                                foo = "Item not found!";
                        }
                }
                return foo;
        }

        public String parseXmlSlots() {
                String foo = "";
                String backup = "Other";
                if (!query.isEmpty()) {
                        String type = parseXmlType();
                        if (!isErrorXml(root).equalsIgnoreCase("Item not found!")) {
                                foo = findElement(root, "class");
                                if (type.toString().contains("vanquisher") || type.toString().contains("conqueror") || type.toString().contains("protector")) {
                                        foo = "Tier";
                                } else {
                                        String temp = findElement(root, "inventorySlot").replace("-", "").replace(" ", "").replace("HeldInOffhand", "OffHand");
                                        if (!temp.isEmpty()) {
                                                foo = temp;
                                        }
                                        String s = checkEnum(foo);
                                        if (s.isEmpty()) {
                                                foo = backup;
                                        } else {
                                                foo = s;
                                        }
                                }
                        } else {
                                foo = "Item not found!";
                        }
                }
                return foo;
        }

        public String checkEnum(String str) {
                for (Slots me : Slots.values()) {
                        if (me.name().equalsIgnoreCase(str)) {
                                return me.toString();
                        }
                }
                return "";
        }

        public String parseXmlUrl() {
                return findElement(root, "link");
        }

        public String parseXmlName() {
                return findElement(root, "name");
        }

        public String parseXmlQuality() {
                return findElement(root, "quality");
        }

        public String parseXmlItemLevel() {
                return findElement(root, "level");
        }
}
