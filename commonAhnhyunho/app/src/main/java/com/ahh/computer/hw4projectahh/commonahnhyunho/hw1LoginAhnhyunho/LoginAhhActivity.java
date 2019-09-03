/*
* Hansung Univ. Computer Engineering, 1492024 안현호
* */

/*
* Hansung Univ. Computer Engineering
* android project HW 1.0
* 1492024 안현호
*/

package com.ahh.computer.hw4projectahh.commonahnhyunho.hw1LoginAhnhyunho;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.ahh.computer.hw4projectahh.commonahnhyunho.R;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.DatabaseBroker;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.User;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.*;
import com.ahh.computer.hw4projectahh.commonahnhyunho.hw2BookingAhnhyunho.BookingAhhActivity;
import com.ahh.computer.hw4projectahh.commonahnhyunho.hw3ManagerAhnhyunho.ManagingGroupAhhActivity;
import com.ahh.computer.hw4projectahh.commonahnhyunho.hw3ManagerAhnhyunho.ManagingUserAhhActivity;

import java.util.ArrayList;


public class LoginAhhActivity extends AppCompatActivity {

    private Context context;
    private String rootPath;
    private DatabaseBroker dataBaseBroker;
    private Message message;    // 메세지 호출을 위한 객체
    private RadioGroup radioGroup; // 사용자/관리자 radio 버튼
    private EditText idField; // id 필드
    private EditText passwardField; // password 필드
    private Spinner groupSpinner; // 소속그룹스피너
    private Button changePasswordButton;
    private Button exitButton;
    private Button cofirmButton;
    private ArrayList<String> groupArrayList;
    private ArrayList<User> userArrayList;
    private int groupPosition;
    private int radioGroupMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ahh);
        context= this;
        rootPath = "AhhDb";
        setTitle("로그인");
        message = new Message(); // 메세지 호출을 위한 객체
        radioGroup = findViewById(R.id.radioButton); // 사용자/관리자 radio 버튼
        idField = findViewById(R.id.idField); // id 필드
        passwardField = findViewById(R.id.passwordField); // password 필드
        groupSpinner = findViewById(R.id.groupSpinner); // 소속그룹스피너
        changePasswordButton = findViewById(R.id.changePasswordButton);
        exitButton =findViewById(R.id.exitButton);
        cofirmButton = findViewById(R.id.cofirmButton);

        //groupPosition =0;// default값 : 제1스터디그룹
        //radioGroupMode = 0;// default값 : 사용자



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {    // 사용자/관리자 버튼에 관련한 ID필드값 자동입력

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.admin){
                    radioGroupMode=0;
                    idField.setText("root");
                    passwardField.setText("");
                    setUseableEditText(idField,false);
                }
                else if(checkedId==R.id.user){
                    radioGroupMode=1;
                    idField.setText("");
                    passwardField.setText("");
                    setUseableEditText(idField,true);
                }
            }
        });


        //----------------------------------------------------------------------------------------------------------------------------
        // 소속그룹 변경

        dataBaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        dataBaseBroker.setGroupOnDataBrokerListener(this, onGroupDatabaseListener);
        //----------------------------------------------------------------------------------------------------------------------------


        //----------------------------------------------------------------------------------------------------------------------------
        // 사용자 데이터베이스 리딩
        dataBaseBroker.setUserOnDataBrokerListener(this, onUserDatabaseListener);
        //----------------------------------------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------------------------------------
        //비밀번호 변경 버튼
        changePasswordButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                int flag = 0; // flag : flag 값이 0이면, 일치하는 아이디가 없는 걸 의미.
                for(int i =0;i<userArrayList.size();i++) {
                    if (userArrayList.get(i).isMeByName(idField.getText().toString())) {
                        flag++; // 아이디 존재
                        if (userArrayList.get(i).isMeByPassword(passwardField.getText().toString())) {
                            if (userArrayList.get(i).isMeByName("root")){
                                // (root 사용자이기때문에 그룹검사 미실행.)
                                message.information(LoginAhhActivity.this, "경고", "관리자는 비밀번호 변경이 불가능합니다.");
                            }
                            else {
                                if (userArrayList.get(i).isMeByGroup(groupSpinner.getSelectedItem().toString())) {
                                    // 로그인 정보 일치
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("'" + idField.getText().toString() + "'의 비밀번호 변경");
                                    LayoutInflater inflater = getLayoutInflater();
                                    ConstraintLayout constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.dialog_changepassword, null);
                                    final EditText newPassword = constraintLayout.findViewById(R.id.newPassword);
                                    final EditText newPasswordCf = constraintLayout.findViewById(R.id.newPasswordCf);
                                    final int finalI = i;
                                    final String nameBuffer = userArrayList.get(i).userName;
                                    final String groupBuffer = userArrayList.get(i).userGroup;
                                    builder.setView(constraintLayout);
                                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() { // 다이얼로그 확인버튼 리스너
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String newPasswordStr = newPassword.getText().toString();
                                            final String newPasswordCfStr = newPasswordCf.getText().toString();
                                            if(newPasswordStr.equals(newPasswordCfStr)){   // for문을 이상없이 통과하면
                                                //비밀번호 변경 후 db업데이트
                                                userArrayList.remove(finalI);   // 현재정보 지우기
                                                User newUser = new User(nameBuffer, newPasswordStr, groupBuffer);   //갱신된 비밀번호로 새로운 user생성
                                                userArrayList.add(newUser); // db에 새로운 user 추가
                                                dataBaseBroker.saveUserDatabase(context, userArrayList); // db업데이트

                                            }
                                            else{   // 동일한 그룹명이 있으면
                                                Message.information(context, "오류","비밀번호가 동일하지 않습니다.");
                                            }
                                        }
                                    });
                                    builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
                                    builder.create().show();

                                } else {
                                    // 그룹 불일치
                                    message.information(LoginAhhActivity.this, "경고", "소속그룹이 불일치합니다.");
                                }
                            }
                        } else {
                            //password 불일치
                            message.information(LoginAhhActivity.this, "경고", "비밀번호가 불일치합니다.");
                        }
                    }
                }
                if(flag==0){    // 일치하는 아이디가 없을 때
                    message.information(LoginAhhActivity.this, "경고", "일치하는 아이디가 없습니다.");
                }
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------


        //----------------------------------------------------------------------------------------------------------------------------
        //끝내기 버튼
        exitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();   // 앱 종료
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------


        //----------------------------------------------------------------------------------------------------------------------------
        //확인 버튼
        cofirmButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // 입력된 내용(소속그룹, 아이디, 비밀번호)를 사용자 데이터베이스 내용과 비교하여 로그인 허가, 불허를 결정한다.
                // 사용자가 root인 경우 소속그룹은 따지지 않는다.

                int flag = 0; // flag : flag 값이 0이면, 일치하는 아이디가 없는 걸 의미.
                for(int i =0;i<userArrayList.size();i++) {
                    if (userArrayList.get(i).isMeByName(idField.getText().toString())) {
                        flag++; // 아이디 존재
                        if (userArrayList.get(i).isMeByPassword(passwardField.getText().toString())) {
                            if (userArrayList.get(i).isMeByName("root")){
                                // 로그인 성공 (root 사용자이기때문에 그룹검사 미실행.)
                                Intent intent;
                                intent =new Intent(LoginAhhActivity.this, ManagingGroupAhhActivity.class);
                                 intent.putExtra("dbName",rootPath);
                                startActivity(intent);
                                //finish();

                                //message.information(LoginAhhActivity.this, "경고", "root로그인 하였습니다.");
                            }
                            else {
                                if (userArrayList.get(i).isMeByGroup(groupSpinner.getSelectedItem().toString())) {
                                    // 로그인 성공
                                    Intent intent;
                                    intent =new Intent(LoginAhhActivity.this, BookingAhhActivity.class);
                                    intent.putExtra("userName",idField.getText().toString());
                                    intent.putExtra("userGroup",groupSpinner.getSelectedItem().toString());
                                    intent.putExtra("dbName",rootPath);
                                    startActivity(intent);
                                    //finish();

                                    //message.information(LoginAhhActivity.this, "경고", "user로그인 하였습니다.");
                                } else {
                                    // 그룹 불일치
                                    message.information(LoginAhhActivity.this, "경고", "소속그룹이 불일치합니다.");
                                }
                            }
                        } else {
                            //password 불일치
                            message.information(LoginAhhActivity.this, "경고", "비밀번호가 불일치합니다.");
                        }
                    }
                }
                if(flag==0){    // 일치하는 아이디가 없을 때
                    message.information(LoginAhhActivity.this, "경고", "일치하는 아이디가 없습니다.");
                }
            }
        });
        //----------------------------------------------------------------------------------------------------------------------------

        //----------------------------------------------------------------------------------------------------------------------------
        //preference로부터 데이터 읽어오기
        if(savedInstanceState==null){
            SharedPreferences prefs = getSharedPreferences("login_info",0);
            String id= prefs.getString("id","");
            String pass= prefs.getString("password","");
            groupPosition = prefs.getInt("group",0);    // groupSpinner 포지션 읽어오기
            radioGroupMode = prefs.getInt("mode",0);

            radioGroup.check(radioGroupMode);   // 사용자/관리자 모드 체크
            idField.setText(id);    // id필드 채우기
            passwardField.setText(pass);    //password필드 채우기
        }
        //----------------------------------------------------------------------------------------------------------------------------

    }   // Oncreate()

    //----------------------------------------------------------------------------------------------------------------------------
    // 그룹스피너 관련 메소드
    DatabaseBroker.OnDataBrokerListener onGroupDatabaseListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            groupArrayList = dataBaseBroker.loadGroupDatabase(LoginAhhActivity.this);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginAhhActivity.this,android.R.layout.simple_list_item_1,groupArrayList);
            groupSpinner.setAdapter(adapter);

            groupSpinner.setSelection(groupPosition);   // groupSpinner 포지션 지정
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    DatabaseBroker.OnDataBrokerListener onUserDatabaseListener = new DatabaseBroker.OnDataBrokerListener(){
        @Override
        public void onChange(String databaseStr) {
            userArrayList = dataBaseBroker.loadUserDatabase(LoginAhhActivity.this);
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 사용자모드 선택 관련 메소드
    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    //preference를 위한 onDestory()
    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = getSharedPreferences("login_info",0);
        SharedPreferences.Editor editor = prefs.edit();

        String id = idField.getText().toString();
        String pass = passwardField.getText().toString();
        int group = groupSpinner.getSelectedItemPosition();
        int mode = radioGroup.getCheckedRadioButtonId();  // radioGroupMode=0이면 사용자, 1이면 관리자

        editor.putString("id",id);
        editor.putString("password",pass);
        editor.putInt("group",group);
        editor.putInt("mode",mode);
        editor.apply();

    }
}
