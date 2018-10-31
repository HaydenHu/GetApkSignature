package cc.hayden.gas;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;



public class AboutActivity extends Activity
{
	
	private ApkInfo mApkInfo;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		mApkInfo=new ApkInfo(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		super.onCreateOptionsMenu(menu);
		menu.add(0,1,0,"QQ群");
		//menu.add(0,2,0,"退出");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO: Implement this method
		switch(item.getItemId())
		{
			case 1:
				mApkInfo. joinQQGroup("FZVvJOpb6oXJgQ0RYsC7ETz5mAxzRety");
				break;
			case 2:
				break;
			default:return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	
}
