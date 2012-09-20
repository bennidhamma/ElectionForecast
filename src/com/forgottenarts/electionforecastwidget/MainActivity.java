package com.forgottenarts.electionforecastwidget;

import com.forgottenarts.electionforecastwidget.R;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.forgottenarts.electionforecastwidget.MESSAGE";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        LinearLayout layout = new LinearLayout (this);
        try
		{
			Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888); 
			Canvas canvas = new Canvas(bitmap);
			Paint red = new Paint ();
			red.setColor(0xFFCC0000);
			Paint blue = new Paint ();
			blue.setColor (0xFF0099CC);
			canvas.drawRect(0, 0, 100, 100, red);
			int percent = 55;
			canvas.drawRect(0, 0, percent, 100, blue);
			ImageView imageView = new ImageView (this);
			imageView.setImageBitmap(bitmap);
			imageView.setLayoutParams(new LinearLayout.LayoutParams(100,100));
			layout.addView(imageView);
			
		}
		catch(Exception e)
		{
			Log.e("ElectionForecastWidgetProvider", "error with bar", e);
		}
        this.setContentView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
}
