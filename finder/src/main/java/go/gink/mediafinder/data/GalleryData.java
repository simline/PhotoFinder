package go.gink.mediafinder.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import go.gink.mediafinder.config.GalleryConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 加载本地所有图片，可按文件夹分割
 */
public abstract class GalleryData {
	private static final String TAG         = GalleryData.class.getSimpleName();
	private static final Uri MEDIA_URI      = Media.EXTERNAL_CONTENT_URI;
	private static final String[] MEDIA_PROJECTION = {
		Media.DATA,
		Media.DISPLAY_NAME,
		Media.DATE_ADDED,
		Media._ID,
		Media.SIZE
	};

	public class FolderInfo {
		private String	path;
        private String	name;
		private String	cover;
		private int		size;

		public String	getPath()	{ return path; }
		public String	getName()	{ return name; }
		public String	getCover()	{ return cover; }
		public int		getSize()	{ return size; }

		// 当path字段为空时，表示所有目录的集合
		FolderInfo(String path, String coverPath, int size) {
			this.path	= path;
			this.cover	= coverPath;
			this.size	= size;
			this.name	= null == path ? null : new File(coverPath).getParentFile().getName();

			Log.w(TAG, "FolderInfo() ====> path: " + path + "    size: " + size + "    name: " + name);
		}
	}

    public class PhotoInfo {
	    private String	name;
	    private String	path;
	    private String	dir;
	    private long	time;

	    public String	getName()	{ return name; }
	    public String	getPath()	{ return path; }
	    public String	getDir()	{ return dir; }
	    public long		getTime()	{ return time; }

        PhotoInfo(String path, String name, long time) {
            this.path	= path;
            this.name	= name;
            this.time	= time;
	        this.dir	= new File(this.path).getParent();

	        Log.w(TAG, "PhotoInfo() ====> path: " + path + "    dir: " + dir + "    name: " + name);
        }
    }

	private List<FolderInfo> folderInfos    = new ArrayList<>(); // 图片文件夹信息列表
	private List<PhotoInfo> photoInfoList   = new ArrayList<>(); // 图片详细信息列表

	private Context ctx;
	private GalleryConfig config;
	private int loaderID;
	private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback;





	/**
	 * 实例化多媒体资源加载模块
	 */
	public GalleryData(Context ctx, GalleryConfig config, int id) {
		Log.e(TAG, "GalleryData");
		this.ctx        = ctx;
		this.config     = config;
		this.loaderID   = id;
		initPhoto();

		((AppCompatActivity)ctx).getSupportLoaderManager().restartLoader(loaderID, null, mLoaderCallback);
	}

	/**
	 * 外部实例化时需实现的监听器接口，用于接收数据变动提醒
	 */
	public abstract void loadFinished(List<FolderInfo> folderInfos, List<PhotoInfo> photoInfoList);

	/**
	 * 不再使用时，释放资源
	 */
	public void release() {
		((AppCompatActivity)ctx).getSupportLoaderManager().destroyLoader(loaderID);
	}

	/**
	 * 筛选出文件夹下的所有图片
	 *
	 * @param path 需要筛选的文件夹，若为`null`则返回所有图片
	 * @return 对应文件夹下的图片列表
	 */
	public List<PhotoInfo> getMediaByPath(String path) {
		Log.e(TAG, "getMediaByPath");
		List<PhotoInfo> outPhotoInfoList= new ArrayList<>();
		if (null == path)
			outPhotoInfoList.addAll(photoInfoList);
		else
			for (PhotoInfo pi : photoInfoList)
				if(pi.dir.equals(path)) outPhotoInfoList.add(pi);

		return outPhotoInfoList;
	}





    /**
     * 初始化配置
     */
    private void initPhoto() {
	    Log.e(TAG, "initPhoto");
        mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

	        @Override
	        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		        Log.e(TAG, "onCreateLoader");
		        String selection        = null;
		        String selectionArgs[]  = null;
		        String sortOrder        = MEDIA_PROJECTION[2] + " DESC";

		        if (args != null) {
			        String srcPath      = args.getString("path");
			        if (null != srcPath)
				        selection       = MEDIA_PROJECTION[0] + " like '%" + srcPath + "%'";
		        }

		        return new CursorLoader(ctx, MEDIA_URI, MEDIA_PROJECTION, selection, selectionArgs, sortOrder);
	        }

	        @Override
	        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		        Log.e(TAG, "onLoadFinished");
		        if (data == null || data.getCount() == 0 || ! data.moveToFirst())
		        	return;

		        List<PhotoInfo> tempPhotoList = new ArrayList<>();
		        do {
			        int size        = data.getInt(data.getColumnIndexOrThrow(MEDIA_PROJECTION[4]));
			        if (size < 1024 * 5) continue;

			        tempPhotoList.add(new PhotoInfo(
					        data.getString(data.getColumnIndexOrThrow(MEDIA_PROJECTION[0])),
					        data.getString(data.getColumnIndexOrThrow(MEDIA_PROJECTION[1])),
					        data.getLong  (data.getColumnIndexOrThrow(MEDIA_PROJECTION[2]))
			        ));
		        } while (data.moveToNext());

		        parsePhotoList(tempPhotoList);
		        loadFinished(folderInfos, photoInfoList);
	        }

	        @Override
	        public void onLoaderReset(Loader<Cursor> loader) {
		        folderInfos.clear();
		        photoInfoList.clear();

		        loader.abandon();
	        }

			/**
			 * 解析出图片信息列表`photoInfoList` 和相册路径列表`folderInfos`
			 */
			private void parsePhotoList(List<PhotoInfo> tempPhotoList) {
				Log.e(TAG, "parsePhotoList");
				photoInfoList.clear();
				photoInfoList.addAll(tempPhotoList);

				// 增加默认文件夹封面
				folderInfos.clear();
				folderInfos.add(new FolderInfo(null, photoInfoList.get(0).path, photoInfoList.size()));

				HashMap<String, Integer> dirs   = new HashMap<>();  // 统计文件夹下图片文件的个数

				for (PhotoInfo photoInfo : photoInfoList) {
					String photoPath    = photoInfo.getPath();
					String photoDir     = photoInfo.getDir();

					// 增加文件夹封面
					if (! dirs.containsKey(photoDir)) {
						folderInfos.add(new FolderInfo(photoDir, photoPath, 0));
						dirs.put(photoDir, 1);

						// 更新文件夹下的图片文件数量
					} else {
						dirs.put(photoDir, 1 + dirs.get(photoDir));
					}
				}

				// 将文件数量赋值给文件夹，默认文件夹除外
				for (FolderInfo folderInfo : folderInfos)
					if (null != folderInfo.getPath()) folderInfo.size = dirs.get(folderInfo.path);

				// 选中的文件集合，存在外部被删除的情况，则移除该选中项
				for(int i = config.getPaths().size() - 1; i >= 0; i--) {
					String path = config.getPaths().get(i);
					if (! new File(path).exists()) config.getPaths().remove(i);
				}

				// 清理临时数据
				dirs.clear();
			}
        };
    }
}