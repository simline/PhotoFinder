package go.gink.mediafinder.sample.Act;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import go.gink.mediafinder.activity.ActPicker;
import go.gink.mediafinder.config.GalleryConfig;
import go.gink.mediafinder.sample.R;
import go.gink.mediafinder.utils.FileUtils;


public class FormActivity extends AppCompatActivity {
	private static final String TAG			= "FormActivity";

	private RecyclerView photoSender;
	private PhotoSenderAdapter adp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.l_photo_sender);
		Context ctx = this;

		adp			= new PhotoSenderAdapter(ctx);
		photoSender	= findViewById(R.id.photoSender);
		photoSender.setLayoutManager(new GridLayoutManager(ctx, 3));
		photoSender.setAdapter(adp);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PhotoSenderAdapter.GALLERY_INTENT:
				adp.setSelected(data.getStringArrayListExtra("paths"));
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if(hasFocus) adp.notifyDataSetChanged();
	}





	public class PhotoSenderAdapter extends RecyclerView.Adapter<PhotoSenderAdapter.ViewHolder> {
		public static final int GALLERY_INTENT	= 0x0001;
		private String TAG = PhotoSenderAdapter.class.getSimpleName();

		private GalleryConfig config;
		private int maxSize;
		private Context ctx;
		private LayoutInflater inf;

		public class ViewHolder extends RecyclerView.ViewHolder {
			ImageView img;

			public ViewHolder(View itemView) {
				super(itemView);
				img         = itemView.findViewById(R.id.img);

				// 点击任意元素，进入图片选择界面
				img.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openPhotoPicker();
					}
				});

				// 长按元素(非添加按钮)，进入删除状态
				img.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						int position = getAdapterPosition();
						if (isPhotoItem(position)) deleteItem(position);
						return true;
					}
				});
			}
		}

		public PhotoSenderAdapter(Context ctx) {
			this.ctx	= ctx;
			this.inf	= LayoutInflater.from(ctx);

			this.config = new GalleryConfig()
					.setMultiSelect(true, 9)
					// .setCrop(1, 1, 500, 500)
					.setShowCamera(false)
					.setFilePath("/Gallery/Pictures");

			if (TextUtils.isEmpty(config.getProvider())) Log.w(TAG, "Provider is null");

			this.maxSize    = config.getMaxSize();
		}

		public void setSelected(ArrayList<String> paths) {
			this.config.setPaths(paths);
			notifyDataSetChanged();
		}





		private boolean isPhotoItem(int position) {
			int size = config.getPaths().size();
			return config.getPaths().size() > 0 && position < (size < maxSize ? size : maxSize);
		}

		@Override
		public int getItemCount() {
			int size = config.getPaths().size();
			return  0==size ? 1 : (size < maxSize ? 1 + size : maxSize);
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ViewHolder(inf.inflate(R.layout.i_photo_sender, parent, false));
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			String path = null;
			if (isPhotoItem(position))
				path    = config.getPaths().get(position);

			if (null != path)
				Glide.with(ctx).load(path).into(holder.img);
			else
				holder.img.setImageResource(R.drawable.__camera);
		}

		/** 移除指定元素 */
		private void deleteItem(int position) {
			Log.e(TAG, "deleteItem()");
			config.getPaths().remove(position);
			notifyItemRemoved(position);
		}

		private void openPhotoPicker() {
			Log.e(TAG, "openPhotoPicker()");
			FileUtils.createFile(config.getFilePath());

			Intent intent = new Intent(ctx, ActPicker.class);
			intent.putExtra("config", config);
			((Activity)ctx).startActivityForResult(intent, GALLERY_INTENT);
		}
	}
}