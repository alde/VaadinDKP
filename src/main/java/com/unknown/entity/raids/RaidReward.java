package com.unknown.entity.raids;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RaidReward
{

    private int id;
    private String comment;
    private double shares;
    private final List<RaidChar> raidChars = new ArrayList<RaidChar>();
    private int raidId;
    private double original_shares;

    public RaidReward()
    {
    }

    public RaidReward(String comment, int id, int raidId, double shares, double original_shares)
    {
        this.comment = comment;
        this.shares = shares;
        this.id = id;
        this.raidId = raidId;
        this.original_shares = original_shares;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public double getShares()
    {
        return shares;
    }

    public void setShares(double shares)
    {
        this.shares = shares;
    }

    public void setRewardChars(Collection<RaidChar> chars)
    {
        raidChars.addAll(chars);
    }

    public ImmutableList<RaidChar> getRewardChars()
    {
        return ImmutableList.copyOf(raidChars);
    }

    public int getRaidId()
    {
        return raidId;
    }

    public void setRaidId(int raidId)
    {
        this.raidId = raidId;
    }

    public double getOriginalShares()
    {
        return this.original_shares;
    }

    public void setOriginalShares(double originalShares)
    {
        this.original_shares = originalShares;
    }
}
