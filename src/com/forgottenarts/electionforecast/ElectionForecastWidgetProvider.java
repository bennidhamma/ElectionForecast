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
		startDataTask();
	}

	public void startDataTask()
	{
		DataFetcher fetcher = new DataFetcher();
		fetcher.execute();
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

	private ElectionData data;
	private int updateCount = 0;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds)
	{
		updateCount++;
		Log.i("ElectionForecastWidgetProvider", "Update count: " + updateCount);
		startDataTask();
		if (data == null)
			return;
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
		// Update each of the widgets with the remote adapter
		for (int i = 0; i < appWidgetIds.length; ++i)
		{
			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			int appWidgetId = appWidgetIds[i];
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widgetlayout);
			views.setTextViewText(R.id.barackVotes, barackVotes.toString());
			views.setTextViewText(R.id.mittVotes, mittVotes.toString());
			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	public ElectionData getData()
	{
		return data;
	}

	public void setData(ElectionData data)
	{
		this.data = data;
	}

	private class DataFetcher extends AsyncTask<Void, Void, ElectionData>
	{
		@Override
		protected void onPostExecute(ElectionData result)
		{
			setData(result);
		}

		@Override
		protected ElectionData doInBackground(Void... params)
		{
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
				Log.d("DataFetcher", "Length of JSON: " + dataString.length() + " url lengt: " + jsonURL.getContentLength() );
				Log.d("DataFetcher", "JSON" + dataString);
				// need to trim JSON-P prefix and suffix.
				int firstIndex = dataString.indexOf('{');
				int lastIndex = dataString.lastIndexOf('}');
				Log.d("DataFetcher", "First index: " + firstIndex + ", last index: " + lastIndex);
				dataString = dataString.substring(dataString.indexOf('{'));
				dataString = dataString.substring(0,
						dataString.lastIndexOf('}')+1);
				Log.d("DataFetcher", "first char: " + dataString.charAt(0));
				Log.d("DataFetcher", "last char: " + dataString.charAt(dataString.length()-1)); 
				Gson gson = new Gson();
				d = gson.fromJson(dataString, ElectionData.class);
				return d;
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
