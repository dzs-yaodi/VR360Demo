package com.xw.vr360demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.xw.vrlibrary.manager.VR360ConfigBundle;
import com.xw.vrlibrary.util.BitmapUtils;
import com.xw.vrlibrary.util.constant.MimeType;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> stringList = new ArrayList<>();
    private String filePath;
    private int mimeType;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        stringList.add("在线视频");
        stringList.add("本地视频");
        stringList.add("assets视频");
        stringList.add("raw视频");
        stringList.add("网络图片");
        stringList.add("本地图片");
        stringList.add("本地assets图片");

        arrayAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,stringList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){
                    filePath="http://cache.utovr.com/201508270528174780.m3u8";
                    mimeType = MimeType.ONLINE_VIDEO;
                }else if (position == 1){
                    filePath = "/sdcard/test2.mp4";
                    mimeType = MimeType.LOCAL_FILE_VIDEO;
                }else if (position == 2){
                    filePath = "demo_video.mp4";
                    mimeType = MimeType.ASSETS_VIDEO;
                }else if (position == 3){
                    filePath= "android.resource://" + getPackageName() + "/" + R.raw.demo_video;
                    mimeType = MimeType.RAW_VIDEO;
                }else if (position == 4){
                    filePath="https://i.loli.net/2019/09/04/wX5KZYNRF2DBhjc.jpg";
                    mimeType = MimeType.ONLINE_BITMAP;
                }else if (position == 5){
                    filePath = "/sdcard/temp.jpg";
                    mimeType = MimeType.LOCAL_FILE_BITMAP;
                }else if (position == 6){
                    filePath="texture_360_n.jpg";
                    mimeType = MimeType.ASSETS_PICTURE;
                }

                if (mimeType == MimeType.ONLINE_BITMAP){
                    Toast.makeText(MainActivity.this, "正在加载网络图片，请稍后。。。", Toast.LENGTH_SHORT).show();
                    BitmapUtils.loadBitmap(filePath, new BitmapUtils.LoadCallBack() {
                        @Override
                        public void onSucc(Bitmap bitmap) {
                            mBitmap = bitmap;
                            start(true);
                        }

                        @Override
                        public void onFial(String error) {
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else if ( mimeType == MimeType.LOCAL_FILE_BITMAP){
                    mBitmap = BitmapFactory.decodeFile(filePath);
                    start(true);
                }

                if (mimeType != MimeType.ONLINE_BITMAP && mimeType != MimeType.LOCAL_FILE_BITMAP){
                    if (mimeType == MimeType.ASSETS_PICTURE)
                        start(true);
                    else
                        start(false);
                }

            }
        });
    }

    private void start(boolean imageMode) {
        VR360ConfigBundle configBundle = VR360ConfigBundle.newInstance()
                .setFilePath(filePath)
                .setMimeType(mimeType)
                .setRemoveHotspot(true)
//                .setLive(false)
                .setImageModeEnabled(imageMode);

        if (mimeType == MimeType.ONLINE_BITMAP || mimeType == MimeType.LOCAL_FILE_BITMAP){
            configBundle.startEmbeddedActivityWithSpecifiedBitmap(
                    this,mBitmap);
            return;
        }else{
            configBundle.startEmbeddedActivity(this);
        }

    }

}
