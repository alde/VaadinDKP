package com.unknown.entity.character;

public class Adjustment
{

    private int id;
    private int charId;
    private double shares;
    private String date;
    private String comment;
    private double originalShares;

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public double getShares()
    {
        return shares;
    }

    public void setShares(double shares)
    {
        this.shares = shares;
    }

    public int getCharId()
    {
        return charId;
    }

    public void setCharId(int charId)
    {
        this.charId = charId;
    }

    public void setOriginalShares(double originalShares)
    {
        this.originalShares = originalShares;
    }

    public double getOriginalShares()
    {
        return this.originalShares;
    }
}
