package cc.hayden.gas;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;



public class MainActivity extends Activity 
{
	private String sig="";
	private String sigmd5="";
	private ApkInfo mApkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final EditText etPkgName=findViewById(R.id.EditTextPackageName);
		Button btnGetAppInfo=findViewById(R.id.BtnPackageName);
		final EditText etPath=findViewById(R.id.EditTextPath);
		Button btnGetApkInfo=findViewById(R.id.BtnPath);
		final TextView info=findViewById(R.id.TextViewInfo);
		final TextView infoMD5=findViewById(R.id.TextViewMD5);

		mApkInfo=new ApkInfo(this);

		btnGetAppInfo.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					// TODO: Implement this method
					sig = "";
					sigmd5 = "";
					String packageName=etPkgName.getText().toString().trim();

					sig =mApkInfo. getInstalledAPKSignature(getApplicationContext(), packageName);
					if (sig.isEmpty())
					{
						Toast.makeText(getApplicationContext(), "请输入正确的已安装APK包名", Toast.LENGTH_SHORT).show();
					}
					else
					{

						info.setText(sig);
						sigmd5 =mApkInfo. md5(sig);
						infoMD5.setText(sigmd5);

					}
				}
			}
		);

		btnGetApkInfo.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					// TODO: Implement this method
					sig = "";
					sigmd5 = "";
					String path=etPath.getText().toString().trim();

                    //sig=showUninstallAPKSignatures(etPath.getText().toString().trim());//方法一
					File apkFile=new File(path);
					try
					{
						sig = mApkInfo.getSignaturesFromApk(apkFile);//方法二
					}
					catch (IOException e)
					{}
					if(sig.isEmpty())
					{
						sig=mApkInfo. getApkSignatures(getApplicationContext(),apkFile);//方法三
					}

					if (sig.isEmpty())
					{
						Toast.makeText(getApplicationContext(), "请输入正确的APK安装包路径", Toast.LENGTH_SHORT).show();
					}
					else
					{

						info.setText(sig);
						sigmd5 =mApkInfo. md5(sig);
						infoMD5.setText(sigmd5);
					}
				}
			}
		);

		//获取剪贴板管理器：
		final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);



		info.setOnLongClickListener(new View.OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					// TODO: Implement this method
					if (sig.isEmpty())
					{
						Toast.makeText(getApplicationContext(), "还没有获取到签名信息", Toast.LENGTH_SHORT).show();
					}
					else
					{
						cm.setPrimaryClip(ClipData.newPlainText("Label", sig));
						Toast.makeText(getApplicationContext(), "签名已复制到剪切板", Toast.LENGTH_SHORT).show();
					}

					return false;
				}
			});
		infoMD5.setOnLongClickListener(new View.OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					// TODO: Implement this method
					if (sigmd5.isEmpty())
					{
						Toast.makeText(getApplicationContext(), "还没有获取到签名信息", Toast.LENGTH_SHORT).show();
					}
					else
					{
						cm.setPrimaryClip(ClipData.newPlainText("Label", sigmd5));
						Toast.makeText(getApplicationContext(), "签名MD5已复制到剪切板", Toast.LENGTH_SHORT).show();
					}

					return false;
				}
			});


    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO: Implement this method
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO: Implement this method
		//return super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
            case R.id.about:
                //Toast.makeText(this, "add_item", Toast.LENGTH_SHORT).show();
				Intent intent=new Intent();
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);

                break;
            case R.id.qq:

                mApkInfo.joinQQGroup("FZVvJOpb6oXJgQ0RYsC7ETz5mAxzRety");
                break;
			case R.id.exit:
                finish();
                break;
        }
        return true;
	}



}
