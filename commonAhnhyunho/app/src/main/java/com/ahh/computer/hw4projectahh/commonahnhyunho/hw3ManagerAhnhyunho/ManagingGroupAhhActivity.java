package com.ahh.computer.hw4projectahh.commonahnhyunho.hw3ManagerAhnhyunho;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.DatabaseBroker;
import com.ahh.computer.hw4projectahh.commonahnhyunho.R;

import java.util.ArrayList;

public class ManagingGroupAhhActivity extends AppCompatActivity {
    DatabaseBroker databaseBroker;
    Context context;
    ListView listView;
    Button btnAdd;
    String rootPath;
    private ArrayList<String> groupArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managing_group_activity);
        setTitle("그룹관리");
        rootPath = getIntent().getStringExtra("dbName");
        context = this;
        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(addBtnListener);  // 리스너 등록

        listView.setOnItemLongClickListener(longClickListener);
        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setGroupOnDataBrokerListener(this, onGroupDatabaseListener);
    }

    //----------------------------------------------------------------------------------------------------------------------------
    // 메뉴 출력 관련
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        menu.findItem(R.id.groupMenu).setEnabled(false);
        menu.findItem(R.id.groupMenu).setIcon(R.drawable.group_using);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.userMenu: //사용자관리
                intent = new Intent(this,ManagingUserAhhActivity.class);
                intent.putExtra("dbName",rootPath);
                startActivity(intent);
                finish();
                return true;
            case R.id.settingMenu: //설정관리
                intent = new Intent(this,ManagingSettingsAhhActivity.class);
                intent.putExtra("dbName",rootPath);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // LongClick 리스너 - 지우는 용도
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("알림");
            builder.setMessage("'" + groupArrayList.get(position).toString() + "'그룹을 정말로 삭제하기 원하십니까?");

            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {  // 다이얼로그 확인버튼 리스너
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    groupArrayList.remove(position);
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) adapterView.getAdapter();
                    adapter.notifyDataSetChanged();
                    databaseBroker.saveGroupDatabase(context,groupArrayList);
                }
            });
            builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
            builder.create().show();

            return true;
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------------------------------------
    // DatabaseBroker 리스너
    DatabaseBroker.OnDataBrokerListener onGroupDatabaseListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupArrayList = databaseBroker.loadGroupDatabase(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,groupArrayList);
            listView.setAdapter(adapter);
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------------------
    // 버튼 리스너 - 새로운 그룹 추가
    View.OnClickListener addBtnListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("그룹생성");
            LayoutInflater inflater = getLayoutInflater();
            ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.dialog_addgroup, null);
            final EditText newGroupName = constraintLayout.findViewById(R.id.newGroupName);
            builder.setView(constraintLayout);

            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() { // 다이얼로그 확인버튼 리스너
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String newGroupNameStr = newGroupName.getText().toString();
                    int i = 0;
                    for(i = 0 ; i< groupArrayList.size();i++){
                        if(groupArrayList.get(i).equals(newGroupNameStr))
                            break;  // 동일한 그룹명이면 for문 탈출
                    }
                    if(i==groupArrayList.size()){   // for문을 이상없이 통과하면
                        groupArrayList.add(newGroupNameStr);
                        databaseBroker.saveGroupDatabase(context,groupArrayList);
                    }
                    else{   // 동일한 그룹명이 있으면
                        AlertDialog.Builder builder2_fail = new AlertDialog.Builder(context);    // 불가능 알람을 위한 새로운 다이얼로그를 위한 빌더
                        builder2_fail.setTitle("경고");
                        builder2_fail.setMessage("이미 있는 그룹명입니다.");
                        builder2_fail.create().show();
                    }
                }
            });
            builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.

            // 여기
            builder.create().show();
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
                ManagingGroupAhhActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
        builder.create().show();
    }
    //----------------------------------------------------------------------------------------------------------------------------
}
