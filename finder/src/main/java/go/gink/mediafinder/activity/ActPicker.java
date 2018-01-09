package go.gink.mediafinder.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import go.gink.mediafinder.R;
import go.gink.mediafinder.adapter.AdpFolder;
import go.gink.mediafinder.adapter.AdpPicker;
import go.gink.mediafinder.config.GalleryConfig;
import go.gink.mediafinder.data.GalleryData;
import go.gink.mediafinder.data.GalleryData.FolderInfo;
import go.gink.mediafinder.data.GalleryData.PhotoInfo;
import go.gink.mediafinder.utils.FileUtils;

public class ActPicker extends AppCompatActivity {
	private final static String TAG = "ActPicker";

	private static final int REQUEST_CAMERA	= 100;		// 设置拍摄照片的 REQUEST_CODE
	private static final int REQUEST_CROP   = 101;		// 裁剪图片

	private GalleryConfig config;						// GalleryPick 配置器
	private PopupWindow folderListPopup;				// 文件夹选择弹出框
	private RecyclerView vList;							// 图片列表
	GridLayoutManager gridLayoutManager;

	private File cropTempFile;
	private File cameraTempFile;

	private Context ctx;
	GalleryData imageMedia;

	private TextView back;								// 完成按钮
	private TextView vFolder;							// 文件夹按钮
	private View camera;

	//TODO: ArrayList 线程不安全？
	List<FolderInfo> folderList = new ArrayList<>();	// 相册列表
	List<PhotoInfo>   photoList = new ArrayList<>();	// 相片列表
	private AdpPicker photoPickerAdapter;				// 相片适配器
	private AdpFolder photoFolderAdapter;				// 相册适配器




	/**
	 * 加载特定文件夹下的所有图片
	 */
	private void updatePhotosByFolder(FolderInfo folderInfo) {
		Log.e(TAG, "updatePhotosByFolder()");
		vFolder.setText(null == folderInfo.getName() ? ctx.getString(R.string.gallery_all_folder) : folderInfo.getName());

		photoList.clear();
		photoList.addAll(imageMedia.getMediaByPath(folderInfo.getPath()));
		photoPickerAdapter.notifyDataSetChanged();
		gridLayoutManager.scrollToPosition(0);
	}

	/**
	 * 生成文件夹列表的弹框
	 */
	private PopupWindow getPopupWindow() {
		Log.e(TAG, "getPopupWindow()");
		View pop	   = LayoutInflater.from(ctx).inflate(R.layout.p_photo_folder, null);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx);
		linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		RecyclerView rvFolderList = pop.findViewById(R.id.rvFolderList);
		rvFolderList.setLayoutManager(linearLayoutManager);
		rvFolderList.setAdapter(photoFolderAdapter);

		Point point	 = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		pop.setPadding(0, (int) (point.y * .382), 0, 0);

		PopupWindow win = new PopupWindow(pop, -1, -1, true);
		win.setOutsideTouchable(true);
		win.setAnimationStyle(R.style.popupAnimation);
		pop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				folderListPopup.dismiss();
			}
		});
		return win;
	}

	/**
	 * 生成文件夹上拉选择框的触发按钮相关事件
	 */
	private void buildFolderAdapter() {
		Log.e(TAG, "buildFolderAdapter()");
		photoFolderAdapter = new AdpFolder(ctx, folderList);
		// 文件夹选择框中的项目被点击
		photoFolderAdapter.setOnClickListener(new AdpFolder.OnClickListener() {
			@Override
			public void onClick(FolderInfo folderInfo) {
				updatePhotosByFolder(folderInfo);
				folderListPopup.dismiss();
			}
		});

		// 加载或隐藏文件夹选择框
		vFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (folderListPopup != null && folderListPopup.isShowing()) {
					folderListPopup.dismiss();
					return;
				}
				folderListPopup = getPopupWindow();
				folderListPopup.showAtLocation(vFolder, Gravity.FILL_VERTICAL, 0, 0);
			}
		});
	}

	/**
	 * 更新选取状态信息
	 * 如果是多选状态，显示 已选张数 / 总可选张数
	 */
	private void refreshSelectedStatus() {
		if (! config.isMultiSelect()) {
			finish();
		} else {
			back.setText(getString(R.string.gallery_finish, config.getPaths().size(), config.getMaxSize()));

			// 已选取 Max张图片 后，禁用拍照按钮
			camera.setEnabled(config.getMaxSize() > config.getPaths().size());
		}
	}

	/**
	 * 生成图片选择列表的相关处理事件
	 */
	private void buildPhotosAdapter() {
		Log.e(TAG, "buildPhotosAdapter()");
		photoPickerAdapter = new AdpPicker(ctx, config, photoList);
		photoPickerAdapter.setOnCallBack(new AdpPicker.OnCallBack() {
			@Override
			public void OnPhotoClicked(String path) {
				Log.e(TAG, "OnPhotoClicked:  " + path);
//					if (config.isCrop()) {
//						cameraTempFile = new File(photoPaths.get(0));
//						cropTempFile = FileUtils.getCorpFile(config.getFilePath());
//						cropPicture();
//						return;
//					}
			}

			@Override
			public void OnPhotoSelected() {
				Log.e(TAG, "OnPhotoSelected");
				refreshSelectedStatus();
			}
		});
		//TODO: change `spanCount` to matching the screen size
		gridLayoutManager = new GridLayoutManager(ctx, 3);
		vList.setLayoutManager(gridLayoutManager);
		vList.setAdapter(photoPickerAdapter);
	}





	/**
	 * 剪切图片
	 */
	private void cropPicture() {
//		  UCropUtils.start(mActivity, cameraTempFile, cropTempFile,
//			  config.getAspectRatioX(), config.getAspectRatioY(), config.getMaxWidth(), config.getMaxHeight());
	}

	/** 打开相机拍照
	 *
	 * 如果拍照成功，通过调用'cameraOK()'，生成一张存在于本应用外部存储空间内的图片缓存文件，以时间戳命名
	 * 如果未拍照或拍照失败，通过调用'cameraFailed()'，自动删除缓存文件
	 */
	private void showCameraAction() {
		Log.e(TAG, "showCameraAction()");
		Intent intent	= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (null == intent.resolveActivity(ctx.getPackageManager())) {
			Toast.makeText(ctx, "can't open camera", Toast.LENGTH_LONG).show();
			return;
		}

		cameraTempFile	= FileUtils.createTmpFile("jpg");
		Uri imageUri	= FileProvider.getUriForFile(ctx, config.getProvider(), cameraTempFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		startActivityForResult(intent, REQUEST_CAMERA);
	}

	/**
	 * 拍照成功，重新分配业务流程。此事件由'showCameraAction()'发起。
	 *
	 * 如果需要裁剪，忽略单选多选限制，进入裁剪阶段
	 * 单选状态，直接返回上层界面
	 * 多选状态，更新选中列表并刷新列表，并通知系统扫描该文件夹
	 */
	private void onCameraOK() {
		if (cameraTempFile == null)
			return;
		String path = cameraTempFile.getAbsolutePath();

		// 需要裁剪，忽略单选多选限制
		if (config.isCrop()) {
			Log.e(TAG, "onCameraOK() 需要裁剪，忽略单选多选限制");
			cropTempFile = FileUtils.getCorpFile(config.getFilePath());
			cropPicture();

		// 单选状态，直接返回上层界面
		} else if (! config.isMultiSelect()) {
			Log.e(TAG, "onCameraOK() 单选状态，直接返回上层界面");
			config.getPaths().clear();
			config.getPaths().add(path);
			finish();

		// 多选状态，通知系统扫描该文件夹
		} else {
			config.getPaths().add(path);
			Log.e(TAG, "onCameraOK() 多选状态，通知系统扫描该文件夹  " + config.getPaths().size());

			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = Uri.fromFile(new File(path));
			intent.setData(uri);
			sendBroadcast(intent);
		}
	}

	/** 未拍照或拍照失败，自动删除缓存文件。此事件由'showCameraAction()'发起 */
	private void onCameraFailed() {
		Log.e(TAG, "onCameraFailed()");
		if (cameraTempFile != null && cameraTempFile.exists())
			cameraTempFile.delete();

		if (config.isOpenCamera())
			finish();
	}

	private void onCropOK() {
		Log.e(TAG, "onCropOK()");
//			final Uri resultUri = UCrop.getOutput(data);
//			if (cameraTempFile != null && cameraTempFile.exists()) {
//				cameraTempFile.delete();
//			}
		config.getPaths().add(cropTempFile.getAbsolutePath());
		finish();
	}

	private void onCropFailed() {
		Log.e(TAG, "onCropFailed()");
//		final Throwable cropError = UCrop.getError(data);
	}





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.l_photo_picker);
		this.ctx	= this;

		back		= findViewById(R.id.back);
		vFolder		= findViewById(R.id.vFolder);
		vList		= findViewById(R.id.vList);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		camera = findViewById(R.id.camera);
		camera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.isEnabled()) showCameraAction();
			}
		});



		this.config	 = (GalleryConfig) getIntent().getSerializableExtra("config");

		buildPhotosAdapter();
		buildFolderAdapter();
		refreshSelectedStatus();

		if (imageMedia != null)
			imageMedia.release();

		imageMedia = new GalleryData(ctx, config, 100) {
			@Override
			public void loadFinished(List<FolderInfo> fList, List<PhotoInfo> pList) {
				Log.e(TAG, "loadFinished");
				refreshSelectedStatus();

				photoList.clear();
				photoList.addAll(pList);
				photoPickerAdapter.notifyDataSetChanged();

				folderList.clear();
				folderList.addAll(fList);
				photoFolderAdapter.notifyDataSetChanged();
			}
		};
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e(TAG, "onKeyDown()");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (folderListPopup != null && folderListPopup.isShowing())
				folderListPopup.dismiss();
			else
				finish();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int reqCode, int rspCode, Intent data) {
		if (reqCode == REQUEST_CAMERA) {
			if (rspCode == RESULT_OK) onCameraOK();
			else					  onCameraFailed();
		}

		if (reqCode == REQUEST_CROP) {
			if (rspCode == RESULT_OK) onCropOK();
			else					  onCropFailed();
		}

		super.onActivityResult(reqCode, rspCode, data);
	}

	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putStringArrayListExtra("paths", config.getPaths());
		setResult(RESULT_OK, intent);

		super.finish();
	}
}