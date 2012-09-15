package com.forgottenarts.electionforecast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.forgottenarts.electionforecast.data.ElectionData;
import com.google.gson.Gson;

public class ElectionForecastWidgetProvider extends AppWidgetProvider
{

	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
	}

	public void startDataTask(int[] appWidgetIds)
	{
		DataFetcher fetcher = new DataFetcher();
		fetcher.execute(appWidgetIds);
		fetcher = null;
	}

	private static final int DATE = 0;
	private static final int BARACK_VOTES = 1;
	private static final int MITT_VOTES = 2;
	private static final int BARACK_WIN_PROBABILITY = 3;
	private static final int MITT_WIN_PROBABILITY = 4;
	private static final int BARACK_POPULAR = 5;
	private static final int MITT_POPULAR = 6;
	private static final int UPDATE_TIME = 7;
	
	AppWidgetManager appWidgetManager;
	Context context;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds)
	{
		this.context = context;
		this.appWidgetManager = appWidgetManager;
		startDataTask(appWidgetIds);
	}
	
	public void updateRemoteView (int appWidgetId, ElectionData data)
	{	
		ArrayList<Object> today;
		Number barackVotes = 0, mittVotes = 0;
		try
		{
			today = data.topline.forecast.get(0);
			barackVotes = (Number)today.get(BARACK_VOTES);
			mittVotes = (Number)today.get(MITT_VOTES);
		} catch (Exception e)
		{
			Log.e("ElectionForecastWidgetProvider", "Error parsing today values", e);
		}
		
		RemoteViews views = new RemoteViews(this.context.getPackageName(),
			R.layout.widgetlayout);
		views.setTextViewText(R.id.barackVotes, barackVotes.toString());
		views.setTextViewText(R.id.mittVotes, mittVotes.toString());
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	private class DataFetcher extends AsyncTask<int[], Void, ElectionData>
	{
		private int[] appWidgetIds;
		
		@Override
		protected void onPostExecute(ElectionData result)
		{
			for (int i = 0; i < appWidgetIds.length; ++i)
			{
				updateRemoteView(appWidgetIds[i], result);
			}
		}

		@Override
		protected ElectionData doInBackground(int[]... params)
		{
			appWidgetIds = params[0];
			ElectionData d = null;
			URLConnection jsonURL = null;
			try
			{
				jsonURL = new URL(
						"http://elections.nytimes.com/2012/cards/38-fivethirtyeight-ccol-top.js")
						.openConnection();
			} catch (MalformedURLException e)
			{
				Log.v("ERROR", "MALFORMED URL EXCEPTION");
			} catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
			try
			{
				InputStream in = jsonURL.getInputStream();
				String dataString = convertStreamToString(in);
				// need to trim JSON-P prefix and suffix.
				int firstIndex = dataString.indexOf('{');
				int lastIndex = dataString.lastIndexOf('}');
				dataString = dataString.substring(dataString.indexOf('{'));
				dataString = dataString.substring(0,
						dataString.lastIndexOf('}')+1);
				Gson gson = new Gson();
				d = gson.fromJson(dataString, ElectionData.class);
			} catch (Exception e)
			{
				Log.e("DataFetcher", e.getLocalizedMessage());
			}

			return d;
		}

		private String convertStreamToString(InputStream is)
				throws UnsupportedEncodingException
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			int cp;
			
			try
			{
				while ((cp = reader.read()) != -1)
				{
					sb.append((char)cp);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					is.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
	}
}
