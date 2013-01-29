package com.anbrul.commonfunction;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

public class CommonFuncPackage {
    /**
     * 
     * @param context
     * @param apkPath
     * @return
     */
    public static PackageInfo getApkFileInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

    }

    /**
     * 获取未安装的apk信息
     * 
     * @param ctx
     *            Context
     * @param apkPath
     *            apk路径，可以放在SD卡
     * @return
     */
    public AppInfoData getApkFileInfo(Context ctx, String apkPath, int type) {
        System.out.println(apkPath);
        File apkFile = new File(apkPath);
        if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
            System.out.println("file path is not correct");
            return null;
        }

        AppInfoData appInfoData;
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            // 反射得到pkgParserCls对象并实例化,有参数
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = { String.class };
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = { apkPath };
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            // 从pkgParserCls类得到parsePackage方法
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
            typeArgs = new Class<?>[] { File.class, String.class, DisplayMetrics.class, int.class };
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);

            valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

            // 执行pkgParser_parsePackageMtd方法并返回
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

            // 从返回的对象得到名为"applicationInfo"的字段对象
            if (pkgParserPkg == null) {
                return null;
            }
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

            // 从对象"pkgParserPkg"得到字段"appInfoFld"的值
            if (appInfoFld.get(pkgParserPkg) == null) {
                return null;
            }
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

            // 反射得到assetMagCls对象并实例化,无参
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Object assetMag = assetMagCls.newInstance();
            // 从assetMagCls类得到addAssetPath方法
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            // 执行assetMag_addAssetPathMtd方法
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

            // 得到Resources对象并实例化,有参数
            Resources res = ctx.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            // 这个是重点
            // 得到Resource对象后可以有很多用处
            res = (Resources) resCt.newInstance(valueArgs);

            // 读取apk文件的信息
            appInfoData = new AppInfoData();
            if (info != null) {
                if (info.icon != 0) {
                    // 图片存在，则读取相关信息
                    Drawable icon = res.getDrawable(info.icon);// 图标
                    appInfoData.icon = icon;
                }
                if (info.labelRes != 0) {
                    String name = (String) res.getText(info.labelRes);// 名字
                    appInfoData.appName = name;
                } else {
                    String apkName = apkFile.getName();
                    appInfoData.appName = apkName.substring(0, apkName.lastIndexOf("."));
                }

                String pkgName = info.packageName;// 包名
                appInfoData.appPackageName = pkgName;
            } else {
                return null;
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                appInfoData.versionName = packageInfo.versionName;// 版本号
                appInfoData.versionCode = packageInfo.versionCode;// 版本码
            }
            return appInfoData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    class AppInfoData{
        Drawable icon;
        String appPackageName;
        String appName;
        int versionCode;
        String versionName;
    }
}
