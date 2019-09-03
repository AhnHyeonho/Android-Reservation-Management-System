package com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by computer on 2019-02-14.
 */

public class Preference {
    static  String defaultRootPath = "gdhong";
    public int      userType;
    public int      userGroup;
    public String   userName;
    public String   userPassword;
    public String   databaseRoot;

    public Preference(){
        userType = 1;
        userGroup = 0;
        userName = "";
        userPassword = "";
        databaseRoot = defaultRootPath;
    }

    public String toString(){
        return userType + "####" + userGroup + "####" + userName + "####" + userPassword + "####" + databaseRoot;
    }

    static public Preference load(Context context){

        Preference preference = new Preference();
        SharedPreferences   preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE);
        preference.userType = preferences.getInt("userType", 1);
        preference.userGroup = preferences.getInt("userGroup", 0);
        preference.databaseRoot = preferences.getString("databaseRoot", defaultRootPath);

        if(preference.userType == 0) {
            preference.userName = preferences.getString("userName", "");
            preference.userPassword = preferences.getString("userPassword", "");
        }else{
            preference.userName = "root";
            preference.userPassword ="";
        }
        return preference;
    }

    static public void save(Context context, Preference preference){
        SharedPreferences   preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("userType",preference.userType);
        editor.putInt("userGroup",preference.userGroup);
        editor.putString("userName",preference.userName);
        editor.putString("userPassword",preference.userPassword);
        editor.putString("databaseRoot",preference.databaseRoot);

        editor.commit();
    }
}
