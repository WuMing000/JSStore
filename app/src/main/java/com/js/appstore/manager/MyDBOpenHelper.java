package com.js.appstore.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBOpenHelper extends SQLiteOpenHelper {

     //定义数据库名和版本号
     private static final String DBNAME = "store_app.db";
     private static final int VERSION = 1;

     public MyDBOpenHelper(Context context) {
         super(context, DBNAME, null, VERSION);
     }

     //创建数据库
     @Override
     public void onCreate(SQLiteDatabase db) {
          //创建数据表
          db.execSQL("create table if not exists UserInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists BannerInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ChoiceInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists WatchInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists BarrageInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists RelaxInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists WorkpieceInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists NewsInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ReadInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists VideoInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists CartoonInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists MessageInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists HotspotInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ReelInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists BroadcastInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists LiveInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists FoodInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ShoppingInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ServeInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PracticalInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists SportInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists HealthInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists InvestmentInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists BringInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists OfficeInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists DocumentInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists CommunicationInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ThoughtInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists InstrumentInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists NetworkDiskInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ClassroomInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists TextualInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists EnglishInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists TranslateInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists TeacherInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ChildrenInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PictureInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PaintInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists EnlightenInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PreschoolInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists MiddleInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ProgrammeInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PlayInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists ChessInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists PuzzleInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
          db.execSQL("create table if not exists CardInformation(appId INTEGER primary key autoincrement,appName varchar(255),appPackage varchar(255),appInformation varchar(255),appIcon varchar(255),appDownLoadURL varchar(255),appIntroduce varchar(2048),appPicture varchar(1024))");
     }

     //升级数据库
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
     }
}