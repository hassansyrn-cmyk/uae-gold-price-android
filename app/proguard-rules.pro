-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Gson reads the API response class and fields reflectively.
-keep class com.uae.goldprice.GoldResponse { *; }

# WorkManager persists the worker class name between app upgrades.
-keep class com.uae.goldprice.GoldPriceWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
