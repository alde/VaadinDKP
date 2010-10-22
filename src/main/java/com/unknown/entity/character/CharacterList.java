/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.character.windows.CharacterInfoWindow;
import com.unknown.entity.character.windows.CharacterEditWindow;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.Role;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bobo
 */
public class CharacterList extends HorizontalLayout implements CharacterInfoListener {

        private DkpList dkpList;
        private CharacterList charList = this;
        CharacterDAO characterDAO;

        public CharacterList(CharacterDAO characherDAO, DkpList dkpList) {
                this.characterDAO = characherDAO;
                this.dkpList = dkpList;
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
                final Button userBtn = new Button(user.toString());
                userBtn.setStyleName(Button.STYLE_LINK);
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
