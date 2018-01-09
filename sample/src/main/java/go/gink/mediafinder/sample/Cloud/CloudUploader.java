//package go.gink.mediafinder.Cloud;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.tencent.cos.model.COSRequest;
//import com.tencent.cos.model.COSResult;
//import com.tencent.cos.model.PutObjectRequest;
//import com.tencent.cos.model.PutObjectResult;
//import com.tencent.cos.task.listener.IUploadTaskListener;
//
//public class CloudUploader extends CloudObject {
//	private static final String TAG     = CloudUploader.class.getSimpleName();
//
//	private String bucket               = "";  // cos空间名称
//	private String cosPath              = "";  // 远端路径，即存储到cos上的路径
//	private String sign                 = "";  // 签名，此处使用多次签名
//
//	private String srcPathes[]          = null;
//	private int progress[]              = null;
//	private PutObjectResult obj[]       = null;
//	private OnProcessListener pl        = null;
//
//
//	public CloudUploader(Context ctx, String bucket, String srcPathes[]) {
//		super(ctx);
//
//		if (null == srcPathes || 0 == srcPathes.length)
//			throw new RuntimeException("file pathes is null or empty");
//
//		this.bucket     = bucket;
//		this.srcPathes  = srcPathes;
//		this.progress   = new int[srcPathes.length];
//	}
//
//	public interface OnProcessListener {
//		void onProcess(int percent[], int percentAvg);
//	}
//
//	public void setOnProcessListener(OnProcessListener processListener) {
//		this.pl = processListener;
//	}
//
//	/**
//	 * 上传图片集合
//	 */
//	public void uploadAll() {
//		for (int i = 0, size = srcPathes.length; i < size; i++)
//			upload(3, i);
//	}
//
//	/**
//	 * 更新整体上传进度，并通知监听器
//	 */
//	private void updateProgress(int index, int percent) {
//		this.progress[index] = percent;
//
//		// 计算总体进度
//		int avg = 0;
//		for (int p : this.progress)
//			avg += p;
//		int avgprogress = avg / srcPathes.length;
//
//		if (null != pl)
//			pl.onProcess(this.progress, avgprogress);
//	}
//
//	/**
//	 * 上传文件至云存储
//	 *
//	 * @param retry     失败后可重复的次数，供函数内递归调用
//	 * @param index     所需上传的文件地址的全局索引
//	 */
//	private void upload(final int retry, final int index) {
//		final String srcPath    = this.srcPathes[index];
//
//		PutObjectRequest putObjectRequest = new PutObjectRequest();
//		putObjectRequest.setBucket(bucket);
//		putObjectRequest.setCosPath(cosPath);
//		putObjectRequest.setSign(sign);
//		putObjectRequest.setSrcPath(srcPath);
//
//		putObjectRequest.setListener(new IUploadTaskListener() {
//			@Override
//			public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
//				updateProgress(index, 100);
//
//				PutObjectResult result = (PutObjectResult) cosResult;
//				if(result != null) {
//					String str =" result => code  : " + result.code + "; msg =" + result.msg +
//								"\n access_url    : " + result.access_url +
//								"\n resource_path : " + result.resource_path +
//								"\n url           : " + result.url;
//					Log.w(TAG, str);
//				}
//			}
//
//			@Override
//			public void onFailed(COSRequest COSRequest, final COSResult cosResult) {
//				updateProgress(index, 0);
//
//				Log.w(TAG,"上传出错： ret =" +cosResult.code + "; msg =" + cosResult.msg);
//			}
//
//			@Override
//			public void onProgress(COSRequest cosRequest, final long currentSize, final long totalSize) {
//				int percent    = (int) (currentSize * 100 / totalSize);
//				updateProgress(index, percent);
//
//				Log.w(TAG,index + "  " + progress + "%");
//				if (retry > 0)
//					upload(retry - 1, index);
//			}
//
//			@Override
//			public void onCancel(COSRequest cosRequest, COSResult cosResult) {}
//		});
//		obj[index]  = this.cosClient.putObject(putObjectRequest);
//	}
//}
