package com.forgottenarts.electionforecast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import com.forgottenarts.electionforecast.data.ElectionData;
import com.forgottenarts.electionforecast.data.ForecastEntry;
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
		ForecastEntry today = null, yesterday = null;
		
		try
		{
			today = new ForecastEntry(data.topline.forecast.get(0));
			
		} catch (Exception e)
		{
			Log.e("ElectionForecastWidgetProvider", "Error parsing today values", e);
		}
		
		try
		{
			yesterday = new ForecastEntry(data.topline.forecast.get(1));
			
		} catch (Exception e)
		{
			Log.e("ElectionForecastWidgetProvider", "Error parsing yesterday values", e);
		}
		
		RemoteViews views = new RemoteViews(this.context.getPackageName(),
			R.layout.widgetlayout);
		
		
		//update text views with today's values.
		views.setTextViewText(R.id.barackVotes, today.getBarackVotes().toString());
		views.setTextViewText(R.id.mittVotes, today.getMittVotes().toString());
		views.setTextViewText(R.id.barackChance, today.getBarackChance().toString() + '%');
		views.setTextViewText(R.id.mittChance, today.getMittChance().toString() + '%');
		views.setTextViewText(R.id.barackPopular, today.getBarackPopular().toString() + '%');
		views.setTextViewText(R.id.mittPopular, today.getMittPopular().toString() + '%');
		
		//changes
		Double barackElectoralChange = today.getBarackVotes() - yesterday.getBarackVotes();
		Double mittElectoralChange = today.getMittVotes() - yesterday.getMittVotes();
		Double barackChanceChange = today.getBarackChance() - yesterday.getBarackChance();
		Double mittChanceChange = today.getMittChance() - yesterday.getMittChance();
		Double barackPopularChange = today.getBarackPopular() - yesterday.getBarackPopular();
		Double mittPopularChange = today.getMittPopular() - yesterday.getMittPopular();
		
		DecimalFormat myFormatter = new DecimalFormat("+#.##;-#.##");
		
		views.setTextViewText(R.id.barackElectoralChange, myFormatter.format(barackElectoralChange));
		views.setTextViewText(R.id.mittElectoralChange,  myFormatter.format(mittElectoralChange));
		views.setTextViewText(R.id.barackChanceChange,  myFormatter.format(barackChanceChange));
		views.setTextViewText(R.id.mittChanceChange,  myFormatter.format(mittChanceChange));
		views.setTextViewText(R.id.barackPopularChange,  myFormatter.format(barackPopularChange));
		views.setTextViewText(R.id.mittPopularChange,  myFormatter.format(mittPopularChange));
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, new Intent(Intent.ACTION_VIEW, 
				Uri.parse("http://fivethirtyeight.blogs.nytimes.com")), 0);
		views.setOnClickPendingIntent(R.id.readMore, pendingIntent);
		
		//bars
		
		try
		{
			double electoralPercent = (today.getBarackVotes() / 538.0 * 100);
			setBitmap (views, R.id.electoralBar, electoralPercent, 100 - electoralPercent);
			setBitmap (views, R.id.chanceBar,  today.getBarackChance(), today.getMittChance());
			setBitmap (views, R.id.popularBar, today.getBarackPopular(), today.getMittPopular());
		}
		catch(Exception e)
		{
			Log.e("ElectionForecastWidgetProvider", "error with bar", e);
		}
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	
	private void setBitmap (RemoteViews views, int id, double bluePercent, double redPercent)
	{
		Bitmap bitmap = Bitmap.createBitmap(200, 10, Bitmap.Config.ARGB_8888); 
		Canvas canvas = new Canvas(bitmap);
		Paint red = new Paint ();
		red.setColor(0xFFCC0000);
		Paint blue = new Paint ();
		blue.setColor (0xFF0099CC);
		Paint black = new Paint ();
		black.setColor (0xFF000000);
		canvas.drawRect(200 - (float)redPercent * 2, 0, 200, 10, red);
		
		canvas.drawRect(0, 0, (float)bluePercent * 2, 10, blue);
		canvas.drawLine(100, 0, 100, 10, black);
		
		views.setBitmap(id, "setImageBitmap", bitmap);
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
