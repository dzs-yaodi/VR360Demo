# VR360Demo

[原文链接:https://github.com/Martin20150405/Pano360](https://github.com/Martin20150405/Pano360)

因为项目进度的问题，所以直接在网上找了一个比较好的开源库进行了增删改，基本满足了我的需求。

## 特性

1、页面显示改成单屏。

2、保留了 全景和平面切换，截图功能，进度条相关功能。

3、修改了原库设置bitmap时闪退的问题。

4、切换按钮，截图按钮和标题显示/隐藏设置。

5、支持assets、raw、在线、本地 视频播放。

6、支持assets、在线、本地图片

7、修改MimeType 类型，修改判断方式

## MineType
    //在线
    public static final int ONLINE_BITMAP = 10001;
    public static final int ONLINE_VIDEO = 10002;
    //本地
    public static final int LOCAL_FILE_BITMAP = 10003;
    public static final int LOCAL_FILE_VIDEO = 10004;
    //系统内部
    public static final int ASSETS_VIDEO=10005;
    public static final int RAW_VIDEO=10006;

    public static final int ASSETS_PICTURE=10007;
    public static final int RAW_PICTURE=10008;

## 使用方法

allprojects {

	repositories { 
		maven { url 'https://jitpack.io' }  
	}
}
dependencies {

       implementation 'com.github.dzs-yaodi:VR360Demo:V1.3'      
}

支持原有的一行代码调用方法(视频)

 VR360ConfigBundle.newInstance().setFilePath(filePath).startEmbeddedActivity(context);
 
也支持自定义VR360ConfigBundle

VR360ConfigBundle configBundle = VR360ConfigBundle.newInstance()<br>
                .setFilePath(filePath)//视频/图片地址<br>
                .setMimeType(mimeType)//视频/图片来源<br>
                .setRemoveHotspot(true)<br>//true-> 去掉水印 ，false -> 显示（默认true）
                .setLive(false)//是否是直播，直播的时候隐藏底部进度条布局<br>
                .setShowVideoTitle(true)//是否显示标题<br>
                .setShowGyroBtn(true)//是否显示陀螺仪按钮<br>
                .setShowScreenshotBtn(true)//是否显示截图按钮<br><br>
                .setImageModeEnabled(imageMode);//true -> 图片，false -> 视频<br>
                
初步在大佬的基础上修改了一下，满足了自己的需求，很多算法还不是很明白，有待继续研究。。。。

