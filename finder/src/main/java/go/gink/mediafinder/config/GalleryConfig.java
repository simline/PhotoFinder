package go.gink.mediafinder.config;

import java.io.Serializable;
import java.util.ArrayList;


public class GalleryConfig implements Serializable {
	private boolean multiSelect		= false;				// 是否开启多选
	private int maxSize				= 9;					// 配置开启多选时 最大可选择的图片数量
	private boolean isShowCamera	= true;					// 是否开启相机
	private String provider			= "go.gink.mediafinder.fileprovider";								// 兼容android 7.0 设置
	private String filePath			= "/Gallery/Pictures";	// 拍照以及截图后 存放的位置

	private ArrayList<String> paths	= new ArrayList<>();	// 已选择照片的路径
	private boolean isOpenCamera	= false;				// 是否直接开启相机

	// 裁剪模块
	private boolean isCrop			= false;				// 是否开启裁剪
	private float aspectRatioX		= 1;					// 裁剪比
	private float aspectRatioY		= 1;					// 裁剪比
	private int maxWidth			= 500;					// 最大的裁剪值
	private int maxHeight			= 500;					// 最大的裁剪值


	public GalleryConfig setMultiSelect(boolean multiSelect, int maxSize) {
		this.multiSelect	= multiSelect;
		this.maxSize		= maxSize;
		return this;
	}

	public GalleryConfig setOpenCamera(boolean openCamera) {
		this.isOpenCamera = openCamera;
		return this;
	}

	public GalleryConfig setShowCamera(boolean showCamera) {
		this.isShowCamera = showCamera;
		return this;
	}

	public GalleryConfig setProvider(String provider) {
		this.provider = provider;
		return this;
	}

	public GalleryConfig setFilePath(String filePath) {
		this.filePath = filePath;
		return this;
	}

	public GalleryConfig setCrop(float aspectRatioX, float aspectRatioY, int maxWidth, int maxHeight) {
		this.aspectRatioX	= aspectRatioX;
		this.aspectRatioY	= aspectRatioY;
		this.maxWidth		= maxWidth;
		this.maxHeight		= maxHeight;
		this.isCrop			= true;
		return this;
	}

	public boolean isMultiSelect() {
		return multiSelect;
	}
	public int getMaxSize() {
		return maxSize;
	}

	public String getProvider() {
		return provider;
	}
	public String getFilePath() {
		return filePath;
	}

	public ArrayList<String> getPaths() {
		return paths;
	}
	public void setPaths(ArrayList<String> paths) {
		this.paths = paths;
	}

	public boolean isOpenCamera() {
		return isOpenCamera;
	}
	public boolean isShowCamera() {
		return isShowCamera;
	}



	public boolean isCrop() {
		return isCrop;
	}
	public float getAspectRatioX() {
		return aspectRatioX;
	}
	public float getAspectRatioY() {
		return aspectRatioY;
	}
	public int getMaxWidth() {
		return maxWidth;
	}
	public int getMaxHeight() {
		return maxHeight;
	}
}