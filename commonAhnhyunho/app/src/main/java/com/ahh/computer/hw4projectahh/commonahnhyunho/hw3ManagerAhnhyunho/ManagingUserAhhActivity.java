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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ahh.computer.hw4projectahh.commonahnhyunho.R;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.DatabaseBroker;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.User;
import com.ahh.computer.hw4projectahh.commonahnhyunho.hw2BookingAhnhyunho.BookingAhhActivity;

import java.util.ArrayList;

public class ManagingUserAhhActivity extends AppCompatActivity {
    DatabaseBroker databaseBroker;
    Context context;
    ListView listView;
    Button btnAdd;
    private ArrayList<User> userArrayList;
    private ArrayList<String> groupArrayList;
    String rootPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managing_user_activity);
        setTitle("사용자관리");
        rootPath = getIntent().getStringExtra("dbName");
        context = this;
        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(addBtnListener);  // 리스너 등록

        listView.setOnItemLongClickListener(longClickListener);
        databaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        databaseBroker.setUserOnDataBrokerListener(this, onUserDatabaseListener);
        databaseBroker.setGroupOnDataBrokerListener(this, onGroupDatabaseListener);

    }

    //----------------------------------------------------------------------------------------------------------------------------
    // 메뉴 출력 관련
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        menu.findItem(R.id.userMenu).setEnabled(false);
        menu.findItem(R.id.userMenu).setIcon(R.drawable.user_using);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.groupMenu: //그룹관리
                intent = new Intent(this,ManagingGroupAhhActivity.class);
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
    DatabaseBroker.OnDataBrokerListener onUserDatabaseListener = new DatabaseBroker.OnDataBrokerListener(){
        @Override
        public void onChange(String databaseStr) {
            userArrayList = databaseBroker.loadUserDatabase(context);
            UserAdapter adapter = new UserAdapter(context, userArrayList);
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
            builder.setTitle("사용자생성");
            LayoutInflater inflater = getLayoutInflater();
            ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.dialog_adduser, null);
            final EditText newName = constraintLayout.findViewById(R.id.userName);
            final EditText newPass = constraintLayout.findViewById(R.id.userPw);
            final Spinner newGroup = constraintLayout.findViewById(R.id.userGroup);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,groupArrayList);
            newGroup.setAdapter(adapter);



            builder.setView(constraintLayout);

            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() { // 다이얼로그 확인버튼 리스너
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String newNameStr = newName.getText().toString();
                    if(newName.getText().toString().equals("")||newPass.getText().toString().equals("")||newGroup.getSelectedItem().toString().equals("")){ // 빈칸 검사 조건 - 빈칸이 있으면 생성 불가
                        AlertDialog.Builder builder2_fail = new AlertDialog.Builder(context);    // 불가능 알람을 위한 새로운 다이얼로그를 위한 빌더
                        builder2_fail.setTitle("경고");
                        builder2_fail.setMessage("빈 칸이 존재합니다.");
                        builder2_fail.create().show();
                    }
                    else {
                        int i = 0;
                        for (i = 0; i < userArrayList.size(); i++) {
                            if (userArrayList.get(i).equals(newNameStr))
                                break;  // 동일한 id가 있으면 for문 탈출
                        }
                        if (i == userArrayList.size()) {   // for문을 이상없이 통과하면
                            User newUser = new User(newName.getText().toString(), newPass.getText().toString(), newGroup.getSelectedItem().toString());
                            userArrayList.add(newUser);
                            databaseBroker.saveUserDatabase(context, userArrayList);    // 새로운 User를 추가 후 database에 저장
                        } else {   // 동일한 id가 있으면
                            AlertDialog.Builder builder2_fail = new AlertDialog.Builder(context);    // 불가능 알람을 위한 새로운 다이얼로그를 위한 빌더
                            builder2_fail.setTitle("경고");
                            builder2_fail.setMessage("이미 있는 id입니다.");
                            builder2_fail.create().show();
                        }
                    }
                }
            });
            builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.

            // 여기
            builder.create().show();
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------
    // LongClick 리스너 - 지우는 용도
    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("알림");
            builder.setMessage("정말로 삭제하기 원하십니까?");

            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {  // 다이얼로그 확인버튼 리스너
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userArrayList.remove(position);
                    UserAdapter adapter = (UserAdapter) adapterView.getAdapter();
                    adapter.notifyDataSetChanged();
                    databaseBroker.saveUserDatabase(context, userArrayList);
                }
            });
            builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
            builder.create().show();

            return true;
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------------------------------------

    class UserAdapter extends BaseAdapter {
        Context context;
        ArrayList<User> myList;
        int selected = -1;

        public UserAdapter(Context context, ArrayList<User> myList) {
            this.context = context;
            this.myList = myList;
        }
        @Override
        public int getCount() {
            return myList.size();
        }
        @Override
        public Object getItem(int i) {
            return myList.get(i);
        }
        @Override
        public long getItemId(int i) {
            return (long)i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.listitem_user, null);
            User user = myList.get(i);
            TextView id = view.findViewById(R.id.textView_id);
            TextView password = view.findViewById(R.id.textView_pw);
            TextView group = view.findViewById(R.id.textView_group);

            id.setText(user.userName);
            password.setText(user.userPassword);
            group.setText(user.userGroup);

            return view;
        }
    }

    DatabaseBroker.OnDataBrokerListener onGroupDatabaseListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupArrayList = databaseBroker.loadGroupDatabase(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,groupArrayList);
        }
    };

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
                ManagingUserAhhActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
        builder.create().show();
    }
    //----------------------------------------------------------------------------------------------------------------------------
}
