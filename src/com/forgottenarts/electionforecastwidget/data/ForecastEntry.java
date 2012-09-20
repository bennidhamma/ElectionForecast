package com.forgottenarts.electionforecastwidget.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ForecastEntry
{
	private static final int DATE = 0;
	private static final int BARACK_VOTES = 1;
	private static final int MITT_VOTES = 2;
	private static final int BARACK_WIN_PROBABILITY = 3;
	private static final int MITT_WIN_PROBABILITY = 4;
	private static final int BARACK_POPULAR = 5;
	private static final int MITT_POPULAR = 6;
	private static final int UPDATE_TIME = 7;
	
	String date;
	Date updateTime;
	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public Date getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(Date updateTime)
	{
		this.updateTime = updateTime;
	}

	public Double getBarackVotes()
	{
		return barackVotes;
	}

	public void setBarackVotes(Double barackVotes)
	{
		this.barackVotes = barackVotes;
	}

	public Double getMittVotes()
	{
		return mittVotes;
	}

	public void setMittVotes(Double mittVotes)
	{
		this.mittVotes = mittVotes;
	}

	public Double getBarackChance()
	{
		return barackChance;
	}

	public void setBarackChance(Double barackChance)
	{
		this.barackChance = barackChance;
	}

	public Double getMittChance()
	{
		return mittChance;
	}

	public void setMittChance(Double mittChance)
	{
		this.mittChance = mittChance;
	}

	public Double getBarackPopular()
	{
		return barackPopular;
	}

	public void setBarackPopular(Double barackPopular)
	{
		this.barackPopular = barackPopular;
	}

	public Double getMittPopular()
	{
		return mittPopular;
	}

	public void setMittPopular(Double mittPopular)
	{
		this.mittPopular = mittPopular;
	}

	Double  barackVotes = 0.0, mittVotes = 0.0,
			barackChance = 0.0, mittChance = 0.0,
			barackPopular = 0.0, mittPopular = 0.0;
	
	public ForecastEntry(ArrayList<Object> source)
	{
		barackVotes = ((Double)source.get(BARACK_VOTES));
		mittVotes = ((Double)source.get(MITT_VOTES));
		barackChance = (Double)source.get(BARACK_WIN_PROBABILITY);
		mittChance = (Double)source.get(MITT_WIN_PROBABILITY);
		barackPopular = (Double)source.get(BARACK_POPULAR);
		mittPopular = (Double)source.get(MITT_POPULAR);
		
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'");
		simpleDateFormatter.setTimeZone(TimeZone.getTimeZone("ET"));
		try
		{
			updateTime = simpleDateFormatter.parse((String)source.get(UPDATE_TIME));
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
