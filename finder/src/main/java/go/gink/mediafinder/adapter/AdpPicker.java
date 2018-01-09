package go.gink.mediafinder.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import go.gink.mediafinder.R;
import go.gink.mediafinder.config.GalleryConfig;
import go.gink.mediafinder.data.GalleryData.PhotoInfo;

import java.util.List;

public class AdpPicker extends RecyclerView.Adapter<AdpPicker.ViewHolder> {
    private final static String TAG     = AdpPicker.class.getSimpleName();

    private Context ctx;
    private LayoutInflater inf;
    private List<PhotoInfo> photoInfoList;                      // 本地照片数据
	private OnCallBack onCallBack;

	//  筛选条件
	private GalleryConfig config;
    private boolean hasMulti;
    private int maxSize;
    private List<String> selectedPhotos;

	public interface OnCallBack {
		/** 选取一张图片后的操作 */
		void OnPhotoSelected();
		/** 选取的图片数量有增减 */
		void OnPhotoClicked(String path);
	}

	public void setOnCallBack(OnCallBack onCallBack) {
		this.onCallBack = onCallBack;
	}

    public AdpPicker(Context ctx, GalleryConfig config, List<PhotoInfo> photoInfoList) {
        this.ctx                = ctx;
        this.inf                = LayoutInflater.from(ctx);
        this.photoInfoList      = photoInfoList;
	    this.config             = config;

        this.selectedPhotos     = config.getPaths();
	    this.hasMulti           = config.isMultiSelect();
	    this.maxSize            = config.getMaxSize();
    }

	@Override
	public int getItemCount() {
		return null == photoInfoList ? 0 : photoInfoList.size();
	}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inf.inflate(R.layout.i_photo_picker, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
	    PhotoInfo photoInfo = photoInfoList.get(position);
		holder.photoInfo	= photoInfo;
	    Glide.with(ctx).load(photoInfo.getPath()).into(holder.ivPhotoImage);

	    // 单选模式，隐藏选择框
	    if (! hasMulti) {
		    holder.chkPhotoSelector .setVisibility(View.GONE);
		    holder.ivPhotoImage     .setSelected(false);

	    // 多选模式，选中样式
	    } else if (selectedPhotos.contains(photoInfo.getPath())) {
		    holder.chkPhotoSelector .setSelected(true);
		    holder.ivPhotoImage     .setSelected(true);

	    // 多选模式，普通样式
	    } else {
		    holder.chkPhotoSelector .setSelected(false);
		    holder.ivPhotoImage     .setSelected(false);
	    }
    }

	class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhotoImage;
        private View chkPhotoSelector;
        private PhotoInfo photoInfo;

		private ViewHolder(View itemView) {
            super(itemView);
            ivPhotoImage        = itemView.findViewById(R.id.ivGalleryPhotoImage);
            chkPhotoSelector    = itemView.findViewById(R.id.chkGalleryPhotoSelector);

	        ivPhotoImage.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View item) {
					String photoPath    = photoInfo.getPath();
		        	// 存在剪切需求时直接操作
					if (config.isCrop()) {
						onCallBack.OnPhotoClicked(photoInfoList.get(getAdapterPosition()).getPath());

					// 单选模式下，直接插入数据
					} else if (!hasMulti) {
						selectedPhotos.clear();
						selectedPhotos.add(photoPath);
						onCallBack.OnPhotoSelected();
					}
		        }
	        });

	        chkPhotoSelector.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View item) {
			        String photoPath    = photoInfo.getPath();

			        // 多选模式下，撤销选中图片
			        if (selectedPhotos.contains(photoPath)) {
				        selectedPhotos.remove(photoPath);
				        notifyItemChanged(getAdapterPosition());
				        onCallBack.OnPhotoSelected();

			        // 多选模式下，增加选中图片
			        } else if (maxSize > selectedPhotos.size()) {
				        selectedPhotos.add(photoPath);
				        notifyItemChanged(getAdapterPosition());
			            onCallBack.OnPhotoSelected();
			        }
		        }
	        });
        }
    }

}