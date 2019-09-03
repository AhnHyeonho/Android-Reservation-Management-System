package com.ahh.computer.hw4projectahh.commonahnhyunho.hw3ManagerAhnhyunho;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ahh.computer.hw4projectahh.commonahnhyunho.R;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.DatabaseBroker;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.Message;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.Settings;

import java.util.ArrayList;

public class ManagingSettingsAhhActivity extends AppCompatActivity {
    DatabaseBroker databaseBroker;
    Context context;
    Settings settings;
    private ArrayList<String> timeStrArrayList; // 스피너에 시간출력을 해주기 위한 ArrayList
    Spinner maxContinue;
    Spinner maxTotal;
    String rootPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managing_settings_activity);
        setTitle("설정관리");
        context = this;
        rootPath = getIntent().getStringExtra("dbName");
        timeStrArrayList = new ArrayList<String>();
        for (int i = 0; i < 21; i++) {  // ArrayList 초기화
            String tmpStr = String.format("%02d %02d", ((i + 1) / 2), ((i + 1) % 2) * 30);
            timeStrArrayList.add(tmpStr);
        }
        maxTotal = findViewById(R.id.maxTotal);
        maxContinue = findViewById(R.id.maxContinue);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, timeStrArrayList);
        maxTotal.setAdapter(adapter);
        maxContinue.setAdapter(adapter);



        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setSettingsOnDataBrokerListener(this, onSettingsDatabaseListener);


        maxTotal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {   // maxTotalBookingSlots에 관한 스피너 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>settings.maxContinueBookingSlots-1) {
                    settings.maxTotalBookingSlots = position + 1;
                    databaseBroker.saveSettingsDatabase(context, settings);
                }
                else{
                    Message.information(context,"경고","1회최대예약시간보다 작을 수 없습니다.");
                    maxTotal.setSelection(settings.maxTotalBookingSlots);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nothing to do
            }
        });
        maxContinue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    // maxContinueBookingSlots에 관한 스피너 리스너
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<settings.maxTotalBookingSlots-1) {
                    settings.maxContinueBookingSlots = position + 1;
                    databaseBroker.saveSettingsDatabase(context, settings);
                }
                else{
                    Message.information(context,"경고","1일최대예약시간을 초과할 수 없습니다.");
                    maxContinue.setSelection(settings.maxContinueBookingSlots);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nothing to do
            }
        });


    }

    //----------------------------------------------------------------------------------------------------------------------------
    // 메뉴 출력 관련
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        menu.findItem(R.id.settingMenu).setEnabled(false);
        menu.findItem(R.id.settingMenu).setIcon(R.drawable.setting_using);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.groupMenu: //그룹관리
                intent = new Intent(this, ManagingGroupAhhActivity.class);
                intent.putExtra("dbName",rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.userMenu: //사용자관리
                intent = new Intent(this, ManagingUserAhhActivity.class);
                intent.putExtra("dbName",rootPath);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // DatabaseBroker 리스너
    DatabaseBroker.OnDataBrokerListener onSettingsDatabaseListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            settings = databaseBroker.loadSettingsDatabase(context);

            maxTotal.setSelection(settings.maxTotalBookingSlots-1);
            maxContinue.setSelection(settings.maxContinueBookingSlots-1);

            Log.i("hhahn", "maxTotal: " + settings.maxTotalBookingSlots + " maxContinue : " + settings.maxContinueBookingSlots);
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 로그아웃을 위한 backKey 관련
    @Override
    public void onBackPressed() {
        //다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("로그아웃");
        builder.setMessage("정말로 로그아웃 하시겠습니까?");

        builder.setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {  // 다이얼로그 확인버튼 리스너
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ManagingSettingsAhhActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
        builder.create().show();
    }
    //----------------------------------------------------------------------------------------------------------------------------
}
