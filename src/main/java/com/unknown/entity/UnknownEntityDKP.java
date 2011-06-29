package com.unknown.entity;

import com.unknown.entity.raids.*;
import com.unknown.entity.character.*;
import com.unknown.entity.items.*;
import com.unknown.entity.panel.AdminPanel;
import com.unknown.entity.panel.TablePanel;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UnknownEntityDKP extends Application implements HttpServletRequestListener {

        private static ThreadLocal<UnknownEntityDKP> threadLocal = new ThreadLocal<UnknownEntityDKP>();
        private Window window;
        private final AdminPanel adminPanel = new AdminPanel();
        private CharacterList charList;
        private DkpList dkpList;
        private ItemList itemList;
        private RaidList raidList;
        private Connection conn = null;
        public String fileName = "Tier12";

        @Override
        public void init() {
                setInstance(this);
                setDatabase("Tier12");
                window = new Window("Unknown Entity DKP");
                setMainWindow(window);
                doDrawings();
                setTheme("dark");
        }

        public static void setInstance(UnknownEntityDKP application) {
                if (getInstance() == null) {
                        threadLocal.set(application);
                        getInstance().conn = new DBConnection().connect();
                }
        }

        public static UnknownEntityDKP getInstance() {
                return threadLocal.get();
        }

        private void doDrawings() {
                dkpList = new DkpList();
                dkpList.attach();

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

        @Override
        public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
                UnknownEntityDKP.setInstance(this);
        }

        @Override
        public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
                threadLocal.remove();
        }

        public void setDatabase(String database) {
                if (getInstance().conn != null)
                        closeDatabase();
                getInstance().fileName = database;
                getInstance().conn = new DBConnection().connect();
        }

        public void closeDatabase() {
                try {
                        getInstance().conn.close();
                } catch (SQLException ex) {
                        Logger.getLogger(UnknownEntityDKP.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        public Connection getConn() {
                return getInstance().conn;
        }
}
