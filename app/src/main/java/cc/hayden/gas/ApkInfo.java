package cc.hayden.gas;

import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.cert.*;
import java.util.jar.*;

import java.security.Signature;
import java.security.cert.Certificate;
import android.app.*;

public class ApkInfo
{
	private Context mContext;
	private Activity mActivity;
	public ApkInfo(Context context)
	{
		this.mContext=context;
	}
	
	//方法一:反射法，适合android 3.0以前版本，3.0以后会失败
	public String showUninstallAPKSignatures(String apkPath)
	{
		String PATH_PackageParser = "android.content.pm.PackageParser";
        try
		{           
			// apk包的文件路径  
			// 这是一个Package 解释器, 是隐藏的           
			// 构造函数的参数只有一个, apk文件的路径          
			// PackageParser packageParser = new PackageParser(apkPath);           
			Class pkgParserCls = Class.forName(PATH_PackageParser);
			Class[] typeArgs = new Class[1];            
			typeArgs[0] = String.class;   
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);   
			Object[] valueArgs = new Object[1];  
			valueArgs[0] = apkPath;   
			Object pkgParser = pkgParserCt.newInstance(valueArgs);  
			// 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况   
			DisplayMetrics metrics = new DisplayMetrics();    
			metrics.setToDefaults();           
			// PackageParser.Package mPkgInfo = packageParser.parsePackage(new       
			// File(apkPath), apkPath,       
			// metrics, 0);          
			typeArgs = new Class[4];          
			typeArgs[0] = File.class;            
			typeArgs[1] = String.class;           
			typeArgs[2] = DisplayMetrics.class;        
			typeArgs[3] = Integer.TYPE;        
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);      
			valueArgs = new Object[4];       
			valueArgs[0] = new File(apkPath);      
			valueArgs[1] = apkPath;          
			valueArgs[2] = metrics;         
			valueArgs[3] = PackageManager.GET_SIGNATURES;    
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);   
			typeArgs = new Class[2];         
			typeArgs[0] = pkgParserPkg.getClass();  
			typeArgs[1] = Integer.TYPE;         
			Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", typeArgs);         
			valueArgs = new Object[2];         
			valueArgs[0] = pkgParserPkg;           
			valueArgs[1] = PackageManager.GET_SIGNATURES;     
			pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs);         
			// 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开       
			Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");    
			Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);      
			if (info == null || info.length < 1)
				return null;
			return info[0].toString();   
		}
		catch (Exception e)
		{    
			e.printStackTrace();      
		}       
		return null;   
	}
	//方法二:使用java的api获取apk文件的签名
	/** 
	 * 从APK中读取签名   
	 * @param file    
	 * @return   
	 * @throws IOException   
	 */  
	public static String getSignaturesFromApk(File file) throws IOException
	{    
		//List<String> signatures=new ArrayList<String>(); 
		StringBuilder sb=new StringBuilder();
		JarFile jarFile=new JarFile(file);  
		try
		{      
			JarEntry je=jarFile.getJarEntry("AndroidManifest.xml");    
			byte[] readBuffer=new byte[8192];    
			Certificate[] certs=loadCertificates(jarFile, je, readBuffer); 
			if (certs != null)
			{ 
				for (Certificate c: certs)
				{   
					String sig=toCharsString(c.getEncoded());   

					//signatures.add(sig);     
					sb.append(sig);
				}      
			}   
		}
		catch (Exception ex)
		{     }     
		return sb.toString();   
	} 
	/**    
	 * 加载签名   
	 * @param jarFile    
	 * @param je 
	 * @param readBuffer   
	 * @return    */  
	private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer)
	{   
		try
		{      
			InputStream is=jarFile.getInputStream(je);    
			while (is.read(readBuffer, 0, readBuffer.length) != -1)
			{       }     
			is.close();      
			return je != null ? je.getCertificates() : null;    
		}
		catch (IOException e)
		{     }    
		return null;   }
	/**   
	 * 将签名转成转成可见字符串  
	 * @param sigBytes   
	 * @return    */ 
	private static String toCharsString(byte[] sigBytes)
	{   
		byte[] sig=sigBytes;     
		final int N=sig.length;    
		final int N2=N * 2;     
		char[] text=new char[N2];    
		for (int j=0; j < N; j++)
		{      
			byte v=sig[j];       
			int d=(v >> 4) & 0xf;      
			text[j * 2] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));    
			d = v & 0xf;       text[j * 2 + 1] = (char)(d >= 10 ? ('a' + d - 10) : ('0' + d));  
		}    
		return new String(text);  
	} 

	//方法三:android4.0以后版本可以直接通过api获取未安装apk的签名
	public static String getApkSignatures(Context context, File apkFile)
	{   
		String sign = "";    
		if (apkFile != null && apkFile.exists())
		{         
			PackageManager pm = context.getPackageManager();   
			PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNATURES);   
			if (pkgInfo != null && pkgInfo.signatures != null && pkgInfo.signatures.length > 0)
			{       
				sign = pkgInfo.signatures[0].toCharsString();        
			}       
		}        
		return sign;  
	}
	public static String getInstalledAPKSignature(Context context, String packageName)
	{   
		PackageManager pm = context.getPackageManager();      
		try
		{          
			PackageInfo appInfo = pm.getPackageInfo(packageName.trim(), PackageManager.GET_SIGNATURES);       
			if (appInfo == null || appInfo.signatures == null)          
				return "";         
			return appInfo.signatures[0].toCharsString();    
		}
		catch (Exception e)
		{           
			// TODO Auto-generated catch block        
			e.printStackTrace();    
		}     
		return "";  
	} 

	public static String md5(String string)
	{   
		if (string.isEmpty())
		{       
			return "";       
		}       
		MessageDigest md5 = null;   
		try
		{       
			md5 = MessageDigest.getInstance("MD5");      
			byte[] bytes = md5.digest(string.getBytes());    
			StringBuilder result = new StringBuilder();          
			for (byte b : bytes)
			{               
				String temp = Integer.toHexString(b & 0xff);      
				if (temp.length() == 1)
				{                   
					temp = "0" + temp;               
				}              
				result.append(temp);          
			}           
			return result.toString();     
		}
		catch (NoSuchAlgorithmException e)
		{       
			e.printStackTrace();      
		}     
		return "";  
	}
	/****************** 发起添加群流程。群号：SketchWare与AIDE(285806975) 的 
	 key 为： FZVvJOpb6oXJgQ0RYsC7ETz5mAxzRety
	 * 调用 joinQQGroup(FZVvJOpb6oXJgQ0RYsC7ETz5mAxzRety)
	 即可发起手Q客户端申请加群 SketchWare与AIDE(285806975)
	 ** @param key 由官网生成的key
	 * @return 返回true表示呼起手Q成功，返回fals表示呼起失败******************/
	public boolean joinQQGroup(String key)
	{   
		Intent intent = new Intent();  
		intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key)); 
		// 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面   
		//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   
		try
		{   
			mContext.startActivity(intent);      
			return true;  
		}
		catch (Exception e)
		{   
			// 未安装手Q或安装的版本不支持     
			return false;    
		}
	}
}
