/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unknown.entity;

import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.dao.RaidDAO;
import com.unknown.entity.database.CharacterDB;
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

/**
 * The Application's "main" class
 */
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
                Drawings();
        }

        private void Drawings() {
                //  ----------- Declarations of Variables
                RaidDAO raidDAO = new RaidDB();
                CharacterDAO characterDAO = new CharacterDB();
                ItemDAO itemDAO = new ItemDB();

                charList = new CharacterList(characterDAO);
                dkpList = new DkpList(characterDAO);
                dkpList.setCharacterList(charList);
                dkpList.addStyleName("small");
                raidList = new RaidList(raidDAO);
                raidList.addStyleName("small");
                itemList = new ItemList(itemDAO);
                itemList.addStyleName("small");

                TablePanel tp = new TablePanel(dkpList, itemList, raidList);
                final HorizontalLayout hzl = tp.HorizontalSegment();

                window.addComponent(adminPanel);
                adminPanel.init();
                adminPanel.setRaidList(raidList);
                adminPanel.setItemList(itemList);
                adminPanel.setCharacterList(charList);
                adminPanel.setDkpList(dkpList);

                // Character List based on Character Class
                characterListOnCharacterClass();

                // DKP Table, Item Table, Raid Table
                window.addComponent(hzl);

        }

        private void characterListOnCharacterClass() {
                HorizontalLayout hzChar = new HorizontalLayout();
                hzChar.addComponent(charList);
                charList.printList();
                window.addComponent(hzChar);
        }

}
