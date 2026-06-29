package com.chenboda01.bcamerav1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.content.Intent;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.OutputStream;

public class MainActivity extends Activity {
    static final int TAKE_PHOTO = 10;
    static final int PICK_PHOTO = 20;
    ImageView preview;
    Bitmap currentBitmap;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(Color.rgb(7, 19, 30));
        scroll.addView(root);
        root.addView(text("B-Camera V1", 30, true));
        root.addView(text("Take photos, preview, save.", 15, false));
        LinearLayout row1 = row();
        row1.addView(btn("Take Photo", v -> takePhoto()));
        row1.addView(btn("Pick Gallery", v -> pickPhoto()));
        root.addView(row1);
        preview = new ImageView(this);
        preview.setBackgroundColor(Color.WHITE);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(430)));
        root.addView(preview);
        LinearLayout row2 = row();
        row2.addView(btn("Save", v -> savePhoto()));
        row2.addView(btn("Clear", v -> clearPhoto()));
        root.addView(row2);
        LinearLayout row3 = row();
        row3.addView(btn("B-Launcher", v -> openApp("com.chenboda01.blauncherv1","com.chenboda01.blauncherv1.MainActivity","B-Launcher")));
        row3.addView(btn("B-Draw", v -> openApp("com.chenboda01.bpaintv1","com.chenboda01.bpaintv1.MainActivity","B-Draw")));
        row3.addView(btn("B-Game", v -> openApp("com.chenboda01.bgamev1","com.chenboda01.bgamev1.MainActivity","B-Game")));
        root.addView(row3);
        root.addView(text("V1 uses the camera app preview. Full-resolution can be V1.1.", 13, false));
        setContentView(scroll);
    }

    TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setPadding(0, 8, 0, 8);
        if (bold) t.setTypeface(null, 1);
        return t;
    }
    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        r.setPadding(0, 10, 0, 10);
        return r;
    }
    Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(56), 1);
        p.setMargins(6, 6, 6, 6);
        b.setLayoutParams(p);
        return b;
    }
    int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    void takePhoto() {
        try { startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PHOTO); }
        catch (Exception e) { toast("Camera app not found."); }
    }
    void pickPhoto() {
        try { startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_PHOTO); }
        catch (Exception e) { toast("Gallery app not found."); }
    }
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null) return;
        try {
            if (request == TAKE_PHOTO) {
                currentBitmap = (Bitmap)data.getExtras().get("data");
                preview.setImageBitmap(currentBitmap);
            } else if (request == PICK_PHOTO) {
                Uri uri = data.getData();
                preview.setImageURI(uri);
                currentBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (Exception e) { toast("Could not load photo."); }
    }
    void savePhoto() {
        if (currentBitmap == null) { toast("No photo to save."); return; }
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "B-Camera-" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= 29) values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/B-Camera");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream out = getContentResolver().openOutputStream(uri);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.close();
            toast("Saved to gallery.");
        } catch (Exception e) { toast("Could not save photo."); }
    }
    void clearPhoto() { currentBitmap = null; preview.setImageDrawable(null); toast("Cleared."); }
    void openApp(String pkg, String cls, String label) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
            if (launch == null) { launch = new Intent(Intent.ACTION_MAIN); launch.addCategory(Intent.CATEGORY_LAUNCHER); launch.setClassName(pkg, cls); }
            startActivity(launch);
        } catch (Exception e) { toast(label + " is not installed yet."); }
    }
    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
}
