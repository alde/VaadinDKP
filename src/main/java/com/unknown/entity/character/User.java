package com.unknown.entity.character;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.Armor;
import com.unknown.entity.Role;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class User implements SiteUser
{

    private final int id;
    private final String name;
    private final Role role;
    private double shares;
    private double dkp_earned;
    private double dkp_spent;
    private double dkp;
    private boolean active = true;
    private Armor armor;
    private int level = 0;
    private String siteusername;
    private final List<CharacterItem> charItems = new ArrayList<CharacterItem>();

    public User(int id, String name, Role role, boolean active, double shares, double dkp_earned, double dkp_spent, double dkp)
    {
        this.id = id;
        this.name = name;
        this.role = role;
        this.shares = shares;
        this.dkp = dkp;
        this.dkp_earned = dkp_earned;
        this.dkp_spent = dkp_spent;
        this.active = active;
        this.armor = role.getArmor();
    }

    public int getId()
    {
        return id;
    }

    public double getShares()
    {
        return shares;
    }

    public void setShares(double x)
    {
        shares = x;
    }

    public void inactivate()
    {
        active = false;
    }

    public void activate()
    {
        active = true;
    }

    public boolean isActive()
    {
        return active;
    }

    public Role getRole()
    {
        return role;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public double getDKP()
    {
        return dkp;
    }

    public double getDKPSpent()
    {
        return dkp_spent;
    }

    public double getDKPEarned()
    {
        return dkp_earned;
    }

    public Armor getArmor()
    {
        return armor;
    }

    public void addCharItems(Collection<CharacterItem> items)
    {
        charItems.addAll(items);
    }

    public ImmutableList<CharacterItem> getCharItems()
    {
        return ImmutableList.copyOf(charItems);
    }

    @Override
    public int getLevel()
    {
        return this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public void setSiteUserName(String name)
    {
        this.siteusername = name;
    }

    @Override
    public String getSiteUserName()
    {
        return this.siteusername;
    }
}
