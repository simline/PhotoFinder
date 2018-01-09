//package go.gink.mediafinder.Cloud;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.tencent.cos.model.COSRequest;
//import com.tencent.cos.model.COSResult;
//import com.tencent.cos.model.GetObjectRequest;
//import com.tencent.cos.model.GetObjectResult;
//import com.tencent.cos.task.listener.IDownloadTaskListener;
//
//public class CloudDownloader extends CloudObject {
//	private static final String TAG     = CloudDownloader.class.getSimpleName();
//
//	private String savePath     = "";  // 本地保存文件的路径
//	private String sign         = "";  // 开启token防盗链了，则需要签名；否则，不需要
//	private OnProcessListener pl;
//
//	public CloudDownloader(Context ctx) {
//		super(ctx);
//	}
//
//	public void setOnProcessListener(OnProcessListener processListener) {
//		this.pl     = processListener;
//	}
//
//	public interface OnProcessListener {
//		void onProgress(int process);
//		void onSuccess(String savePath);
//		void onFailed(String downloadURl);
//	}
//
//	public void download(final int retry, final String downloadURl) {
//		GetObjectRequest getObjectRequest = new GetObjectRequest(downloadURl, savePath);
//		getObjectRequest.setSign(null);
//		getObjectRequest.setListener(new IDownloadTaskListener() {
//			@Override
//			public void onProgress(COSRequest cosRequest, final long currentSize, final long totalSize) {
//				int progress = (int) (currentSize  * 100 / totalSize);
//				if (null != pl)
//					pl.onProgress(progress);
//
//				Log.w(TAG, "progress =" + (int) (progress) + "%");
//			}
//
//			@Override
//			public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
//				if (null != pl)
//					pl.onSuccess(downloadURl);
//
//				Log.w(TAG,"code =" + cosResult.code + "; msg =" + cosResult.msg);
//			}
//
//			@Override
//			public void onFailed(COSRequest COSRequest, COSResult cosResult) {
//				Log.w(TAG,"code =" + cosResult.code + "; msg =" + cosResult.msg);
//
//				if (0 > retry)
//					download(retry - 1, downloadURl);
//				else if (null != pl)
//					pl.onFailed(downloadURl);
//			}
//
//			@Override
//			public void onCancel(COSRequest cosRequest, COSResult cosResult) {}
//		});
//
//		GetObjectResult getObjectResult = cosClient.getObject(getObjectRequest);
//	}
//}
