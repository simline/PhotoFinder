//package go.gink.mediafinder.Cloud;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.tencent.cos.COSClient;
//import com.tencent.cos.COSConfig;
//import com.tencent.cos.common.COSEndPoint;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLEncoder;
//
////TODO: 生成 COSClient 对象
//public class CloudObject {
//	final COSClient cosClient;
//
//	private String appID                = "1254121116";
//	private String persistenceID        = "123";
//
//	public CloudObject(Context ctx) {
//		COSConfig conf  = new COSConfig();
//		conf.setEndPoint(COSEndPoint.COS_SH);
//		cosClient       = new COSClient(ctx, appID, conf, persistenceID);
//	}
//
//	/***
//	 *  签名， 参考官网的签名方式，自己搭建签名服务器；
//	 *  demo中的签名服务器（http://203.195.194.28）只是适合此demo！
//	 *  签名类型：多次签名 ，单次签名
//	 *
//	 * @return 返回多次签名串
//	 */
//	public static String getSign(String bucket){
//		String sign = null;
//		String cgi = "http://203.195.194.28/cosv4/getsignv4.php?" + "bucket=" + bucket + "&service=video";
//		try {
//			URL url = new URL(cgi);
//			HttpURLConnection conn          = (HttpURLConnection) url.openConnection();
//			InputStream in                  = conn.getInputStream();
//			BufferedReader bufferedReader   = new BufferedReader(new InputStreamReader(in));
//			String line                     = bufferedReader.readLine();
//			if(line == null)return  null;
//			JSONObject json = new JSONObject(line);
//			if(json.has("sign")){
//				sign = json.getString("sign");
//			}
//			Log.w("XIAO","sign=" +sign);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return sign;
//	}
//
//	/** @return 返回单次签名串 */
//	public static String getSignOnce(String fileId, String bucket){
//		urlEncoder(fileId);
//		String onceSign = null;
//		String cgi = "http://203.195.194.28/cosv4/getsignv4.php?" + "bucket=" +bucket + "&service=cos&expired=0&path=" + fileId;
//		try {
//			URL url                         = new URL(cgi);
//			HttpURLConnection conn          = (HttpURLConnection) url.openConnection();
//			InputStream in                  = conn.getInputStream();
//			BufferedReader bufferedReader   = new BufferedReader(new InputStreamReader(in));
//			String line                     = bufferedReader.readLine();
//			if(line == null)return  null;
//			JSONObject json = new JSONObject(line);
//			if(json.has("sign")) {
//				onceSign = json.getString("sign");
//			}
//			Log.w("XIAO","onceSign =" + onceSign);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return onceSign;
//	}
//
//	private static String urlEncoder(String fileID){
//		if(fileID == null) return null;
//
//		StringBuilder stringBuilder = new StringBuilder();
//		String[] strFiled = fileID.trim().split("/");
//		int length = strFiled.length;
//		for(int i = 0; i< length; i++){
//			try{
//				String str = URLEncoder.encode(strFiled[i], "utf-8").replace("+","%20");
//				stringBuilder.append(str).append("/");
//			}catch (Exception e){
//				e.printStackTrace();
//			}
//		}
//		if(fileID.startsWith("/")){
//			fileID = "/" + stringBuilder.toString();
//		}else{
//			fileID = stringBuilder.toString();
//		}
//		return fileID;
//	}
//}