/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.Role;
import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bobo
 */
public class CharacterList extends HorizontalLayout implements CharacterInfoListener {

        private DkpList dkpList;
        private CharacterList charList = this;
        CharacterDAO characterDAO;
        private Application app;

        public CharacterList(CharacterDAO characherDAO, DkpList dkpList, Application app) {
                this.characterDAO = characherDAO;
                this.dkpList = dkpList;
                this.app = app;
        }

        private void characterClassImages(List<Role> roles) {
                for (Role r : roles) {
                        VerticalLayout roleList = new VerticalLayout();
                        addComponent(roleList);
                        Embedded e = new Embedded("", new ThemeResource("../ue/img/" + r.toString().toLowerCase() + ".png"));
                        roleList.addComponent(e);
                        addUsersForRole(r, roleList);
                }
        }

        private Button characterListByRole(final User user) {
                Button userBtn = null;
                if (isAdmin() && user.isActive()) {
                        userBtn = new Button(user.toString());
                } else if (isAdmin() && !user.isActive()) {
                        userBtn = new Button("-"+user.toString());
                } else if (!isAdmin() && user.isActive()) {
                        userBtn = new Button(user.toString());
                }
                userBtn.addStyleName(Button.STYLE_LINK);
                userBtn.addListener(new charListClickListener(user));
                return userBtn;
        }

        private void clear() {
                this.removeAllComponents();
        }

        public void printList() {
                clear();
                List<Role> roles = Arrays.asList(Role.values());
                Collections.sort(roles, new ToStringComparator());
                characterClassImages(roles);
        }

        private void addUsersForRole(Role r, VerticalLayout roleList) {
                for (final User user : characterDAO.getUsersWithRole(r)) {

                        Button userBtn = characterListByRole(user);
                        roleList.addComponent(userBtn);

                }
        }

        private boolean isAdmin() {
                final SiteUser siteUser = (SiteUser) app.getUser();
                if (siteUser != null) {
                        return true;
                } else {
                        return false;
                }
        }

        @Override
        public void onCharacterInfoChange() {
                update();
        }

        public void update() {
                printList();
        }

        private static class ToStringComparator implements Comparator<Role> {

                public ToStringComparator() {
                }

                @Override
                public int compare(Role t, Role t1) {
                        return t.toString().compareTo(t1.toString());
                }
        }

        private class charListClickListener implements ClickListener {

                private final User user;

                public charListClickListener(User user) {
                        this.user = user;
                }

                @Override
                public void buttonClick(ClickEvent event) {
                        PopUpControl pop = new PopUpControl(getApplication());
                        pop.setDkpList(dkpList);
                        pop.setCharacterList(charList);
                        pop.showProperCharWindow(user);
                }
        }
}
