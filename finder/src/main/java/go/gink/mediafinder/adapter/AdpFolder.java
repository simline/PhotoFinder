package go.gink.mediafinder.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import go.gink.mediafinder.R;
import go.gink.mediafinder.data.GalleryData.FolderInfo;
import java.util.List;


public class AdpFolder extends RecyclerView.Adapter<AdpFolder.PhotoFolderHolder> {
    private final static String TAG = "AdpFolder";

	private Context ctx;
    private LayoutInflater inf;
    private List<FolderInfo> fInfos;

    private int selector = 0;
    private OnClickListener onClickListener;

    public AdpFolder(Context ctx, List<FolderInfo> fInfos) {
        this.inf        = LayoutInflater.from(ctx);
        this.ctx        = ctx;
        this.fInfos     = fInfos;
    }

	public interface OnClickListener {
		void onClick(FolderInfo folderInfo);
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	@Override
	public int getItemCount() {
		return null == fInfos ? 0 : fInfos.size();
	}

    @Override
    public PhotoFolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoFolderHolder(inf.inflate(R.layout.i_photo_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(PhotoFolderHolder holder, int position) {
	    FolderInfo folderInfo = fInfos.get(position);

	    holder.tvFolderName.setText(0 == position ? ctx.getString(R.string.gallery_all_folder) : folderInfo.getName());
	    holder.tvPhotoNum.setText(ctx.getString(R.string.gallery_photo_num, folderInfo.getSize()));
	    holder.ivIndicator.setVisibility(selector == holder.getAdapterPosition() ? View.VISIBLE : View.GONE);
	    Glide.with(ctx).load(folderInfo.getCover()).into(holder.ivFolderImage);
    }

    class PhotoFolderHolder extends RecyclerView.ViewHolder {
        private ImageView ivFolderImage;
        private TextView tvFolderName;
        private TextView tvPhotoNum;
        private ImageView ivIndicator;

        PhotoFolderHolder(View item) {
            super(item);
            this.ivFolderImage  = item.findViewById(R.id.ivFolderImage);
            this.tvFolderName   = item.findViewById(R.id.tvFolderName);
            this.tvPhotoNum     = item.findViewById(R.id.tvPhotoNum);
            this.ivIndicator    = item.findViewById(R.id.ivIndicator);

	        item.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
			        selector    = getAdapterPosition();
			        onClickListener.onClick(fInfos.get(selector));
		        }
	        });
        }
    }
}