package com.unknown.entity;

import com.unknown.entity.database.CharDB;
import com.unknown.entity.database.ItemDB;
import com.unknown.entity.database.RaidDB;
import com.unknown.entity.raids.*;
import com.unknown.entity.character.*;
import com.unknown.entity.items.*;
import com.unknown.entity.panel.AdminPanel;
import com.unknown.entity.panel.TablePanel;
import com.vaadin.Application;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class UnknownEntityDKP extends Application {

        private Window window;
        private final AdminPanel adminPanel = new AdminPanel();
        private CharacterList charList;
        private DkpList dkpList;
        private ItemList itemList;
        private RaidList raidList;

        @Override
        public void init() {
                window = new Window("Unknown Entity DKP");
                setMainWindow(window);
                doDrawings();
                setTheme("dark");
        }

        private void doDrawings() {
                System.out.println(this.toString());
                dkpList = new DkpList(this);
                dkpList.attach();
                RaidDB.setApplication(this);
                ItemDB.setApplication(this);
                CharDB.setApplication(this);
                charList = new CharacterList(dkpList, this);
                charList.attach();
                dkpList.setCharacterList(charList);
                dkpList.addStyleName("striped");
                raidList = new RaidList();
                raidList.attach();
                raidList.addStyleName("striped");
                itemList = new ItemList();
                itemList.addStyleName("striped");
                raidList.setCharList(charList);
                raidList.setDkpList(dkpList);
                charList.setLists(itemList, raidList);
                dkpList.setLists(itemList, raidList);
                itemList.setLists(charList, dkpList, raidList);
                raidList.setItemList(itemList);

                TablePanel tp = new TablePanel(dkpList, itemList, raidList, this);
                
                final HorizontalLayout hzl = tp.HorizontalSegment();

                window.addComponent(adminPanel);
                adminPanel.init();
                adminPanel.setRaidList(raidList);
                adminPanel.setItemList(itemList);
                adminPanel.setCharacterList(charList);
                adminPanel.setDkpList(dkpList);
                adminPanel.addApplication(this);
                adminPanel.addCharacterInfoListener(charList);
                adminPanel.addCharacterInfoListener(dkpList);
                adminPanel.addItemInfoListener(itemList);
                adminPanel.addRaidInfoListener(raidList);
                
                characterListOnCharacterClass();
                window.addComponent(hzl);

        }

        private void characterListOnCharacterClass() {
                HorizontalLayout hzChar = new HorizontalLayout();
                hzChar.addComponent(charList);
                charList.printList();
                window.addComponent(hzChar);
        }
}
