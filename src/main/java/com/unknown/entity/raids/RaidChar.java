package com.unknown.entity.raids;

public class RaidChar
{

    private int id;
    private int raidid;
    private String name;
    private double shares;

    public int getRaidId()
    {
        return raidid;
    }

    public void setRaidId(int raidid)
    {
        this.raidid = raidid;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getShares()
    {
        return shares;
    }

    public void setShares(double shares)
    {
        this.shares = shares;
    }
}
