

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
	**[] $VALUES;
	public *;
}
-dontwarn com.loc.**


# JavaBean
-keep public class go.gink.mediafinder.data.*
-keepclassmembers class go.gink.mediafinder.data.* {
	public *;
}

