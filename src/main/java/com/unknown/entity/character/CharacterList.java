package com.unknown.entity.character;

import com.unknown.entity.PopUpControl;
import com.unknown.entity.Role;
import com.unknown.entity.database.CharDB;
import com.unknown.entity.items.ItemList;
import com.unknown.entity.raids.RaidList;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharacterList extends HorizontalLayout implements CharacterInfoListener
{

    private DkpList dkpList;
    private CharacterList charList = this;
    private RaidList raidList;
    private ItemList itemList;
    private Application app;

    public CharacterList(DkpList dkpList, Application app)
    {
        this.dkpList = dkpList;
        this.app = app;
    }

    public void setLists(ItemList itemList, RaidList raidList)
    {
        this.itemList = itemList;
        this.raidList = raidList;
    }

    private void characterClassImages(List<Role> roles)
    {
        for (Role r : roles) {
            VerticalLayout roleList = new VerticalLayout();
            addComponent(roleList);
            Embedded e = new Embedded("", new ExternalResource("http://www.unknown-entity.com/images/classes/" + r.
                    toString().toLowerCase() + ".png"));
            e.setType(Embedded.TYPE_IMAGE);
            e.setWidth("100px");
            e.setHeight("20px");
            roleList.addComponent(e);
            addUsersForRole(r, roleList);
        }
    }

    private void clear()
    {
        this.removeAllComponents();
    }

    public void printList()
    {
        clear();
        List<Role> roles = Arrays.asList(Role.values());
        Collections.sort(roles, new ToStringComparator());
        characterClassImages(roles);
    }

    private void addUsersForRole(Role r, VerticalLayout roleList)
    {
        for (final User user : CharDB.getUsersWithRole(r)) {
            if (user.isActive()) {
                NativeButton userBtn = new NativeButton(user.toString());
                userBtn.addStyleName(Button.STYLE_LINK);
                String charclass = user.getRole().toString().replace(" ", "").
                        toLowerCase();
                userBtn.addStyleName(charclass);
                userBtn.addListener(new charListClickListener(user));
                roleList.addComponent(userBtn);
            } else if (isAdmin()) {
                Button userBtn = new Button("--" + user.toString());
                userBtn.addStyleName(Button.STYLE_LINK);
                userBtn.addListener(new charListClickListener(user));
                roleList.addComponent(userBtn);
            }

        }
    }

    private boolean isAdmin()
    {
        final SiteUser siteUser = (SiteUser) app.getUser();
        if (siteUser != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCharacterInfoChange()
    {
        CharDB.clearCache();
        update();
    }

    public void update()
    {
        printList();
    }

    private static class ToStringComparator implements Comparator<Role>
    {

        public ToStringComparator()
        {
        }

        @Override
        public int compare(Role t, Role t1)
        {
            return t.toString().compareTo(t1.toString());
        }
    }

    private class charListClickListener implements ClickListener
    {

        private final User user;

        public charListClickListener(User user)
        {
            this.user = user;
        }

        @Override
        public void buttonClick(ClickEvent event)
        {
            PopUpControl pop = new PopUpControl(getApplication());
            pop.setItemList(itemList);
            pop.setRaidList(raidList);
            pop.setDkpList(dkpList);
            pop.setCharacterList(charList);
            pop.showProperCharWindow(user);
        }
    }
}
