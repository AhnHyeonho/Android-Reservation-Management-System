/*
* Hansung Univ. Computer Engineering, 1492024 안현호
* */

/*
* Hansung Univ. Computer Engineering
* android project HW 2.0
* 1492024 안현호
*/

package com.ahh.computer.hw4projectahh.commonahnhyunho.hw2BookingAhnhyunho;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ahh.computer.hw4projectahh.commonahnhyunho.R;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.DatabaseBroker;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.Message;
import com.ahh.computer.hw4projectahh.commonahnhyunho.commonAhnhyunho.Settings;
import com.ahh.computer.hw4projectahh.commonahnhyunho.hw3ManagerAhnhyunho.ManagingUserAhhActivity;

import java.text.SimpleDateFormat;


public class BookingAhhActivity extends AppCompatActivity {

    private DatabaseBroker dataBaseBroker;
    String rootPath;
    String userName;
    String userGroup;
    Context context;    // mainActivity를 저장할 변수
    int maxSlots = 50;  // 최대 슬롯 개수
    LinearLayout linearLayout;
    BookingDrawer bookingDrawer;
    String bookingDatabase[];
    MyTime timeArray[];
    Settings settingsDatabase;
    MyTextView textViews[]; // bookingDatabase[]로부터 읽어온 정보들을 담는 TextViews들의 배열
    int columnIndex;  // 열 갯수
    SimpleDateFormat currentTime;   // 비교를 위한 현재 시간
    int intCurrentTime; // 현재 시간을 int 형으로 변환한 변수
    static int userCnt = 0;     // Database에 저장된 현재 접속한 user의 예약 횟수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //--------------------------------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_ahh);
        rootPath = getIntent().getStringExtra("dbName");
        userName= getIntent().getStringExtra("userName");
        userGroup=getIntent().getStringExtra("userGroup");
        setTitle("부킹 : " + userGroup);
        //--------------------------------------------------------------
        context = this; // mainActivity를 저장
        textViews = new MyTextView[50];
        setTimeArray();
        settingsDatabase = new Settings();
        linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams llayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, llayoutParams);
        currentTime = new SimpleDateFormat("HHmm");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnIndex = 2;
        } else {
            columnIndex = 4;
        }
        drawBase(columnIndex);

        bookingDrawer = new BookingDrawer();    // 음영처리 및 클릭 불가능 상태 체크를 위한 drawer
        bookingDrawer.start();

        dataBaseBroker = DatabaseBroker.createDatabaseObject(rootPath);
        dataBaseBroker.setBookingOnDataBrokerListener(context, userGroup, dataBrokerListener); // database의 리스너로 dataBrokerListener 지정
        dataBaseBroker.setSettingsOnDataBrokerListener(context, dataBrokerListener); // database의 settingListener 등록
    }

    //----------------------------------------------------------------------------------------------------------------------------
    //  database에 변화가 일어날 때 마다 호출되는 리스너
    DatabaseBroker.OnDataBrokerListener dataBrokerListener = new DatabaseBroker.OnDataBrokerListener() {
        @Override
        public void onChange(String databaseStr) {
            settingsDatabase = dataBaseBroker.loadSettingsDatabase(context);
            bookingDatabase = dataBaseBroker.loadBookingDatabase(context, userGroup);

            for (int i = 0; i < textViews.length; i++) {
                textViews[i].setText(timeArray[i].timeToString() + "                " + bookingDatabase[i]);
            }
        }
    };
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 부킹드로어 클래스 : 현재 부킹 상태(음영처리 및 클릭 불가능 상태 체크)를 그려주는 클래스
    class BookingDrawer extends Thread {
        boolean isRun = true;

        @Override
        public void run() {
            super.run();
            while (isRun) {

                try {
                    sleep(1000);
                } catch (InterruptedException e) {

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < textViews.length; i++) {
                            // 순회하면서 그 시간이 현재시간보다 이전이거나 같으면 음영처리하고 클릭 불가능 상태로 만들어준다.
                            intCurrentTime = Integer.parseInt(currentTime.format(System.currentTimeMillis()));   // 현재 시간을 정수형으로 변경
                            if (timeArray[i].compareTime < intCurrentTime) {
                                textViews[i].setBackgroundColor(Color.GRAY); // 을 음영처리하고 클릭 불가로
                                setUseableTextView(textViews[i], false);
                            }
                        }
                    }
                });

            }
        }
    }
    // 부킹드로어 클래스 끝
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // drawBase() : 원하는 조건으로 외부 슬롯을 그리는 함수

    void drawBase(int colIndex) {
        linearLayout.removeAllViews();

        int row = maxSlots / colIndex;
        if (row * colIndex < maxSlots)
            row += 1;   // 가로가 되서 4줄로 나눌 때 나누는 행이 모자라서 안나오는 view가 있을 수 있으므로 1칸을 더 해준다.

        for (int i = 0; i < row; i++) {
            LinearLayout outerLayout = new LinearLayout(context);
            outerLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            outerLayout.setWeightSum(colIndex);
            linearLayout.addView(outerLayout, outerLp);

            if (i != row - 1) {  // 마지막 행 전 까지는 colIndex만큼씩 출력
                for (int j = 0; j < colIndex; j++) {
                    final MyTextView textViewLeft = new MyTextView(context); // te4xtViews[i*colIndex+j];
                    //textViewLeft.setBackgroundDrawable(getResources().getDrawable(R.drawable.xml_border));
                    textViewLeft.number = i * colIndex + j;
                    textViewLeft.setText(timeArray[textViewLeft.number].timeToString());
                    textViewLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 현재 textView가 비어있으면 로그인 된 이름을 저장하고,
                            // 비어있지 않은 경우 로그인 된 이름과 동일하면 칸을 비우고, 로그인 된 이름과 다르면 아무런 동작을 하지 않는다.
                            MyTextView left = (MyTextView) v;

                            if (bookingDatabase[left.number].equals("")) {    // 비어있으면
                                checkCount(userName);
                                if (userCnt < settingsDatabase.maxTotalBookingSlots) {    // 예약이 가능하면
                                    if (checkCanReservation(left.number, userName)) {  // 연속 3회가 아니면 예약 가능
                                        bookingDatabase[left.number] = userName;    // 이름 넣고
                                        userCnt++;    // 유저 예약횟수 증가
                                    } else {
                                        Message.information(context, "최대 연속예약 횟수 초과", "연속 3회 예약 불가능 합니다.");
                                    }
                                } else {   // 더 이상 예약이 불가능 하면
                                    Message.information(context, "최대 예약 횟수 초과", "하루에 4회 초과예약 불가능 합니다.");
                                }

                            } else {   // 비어있지 않으면
                                checkCount(userName);
                                if (bookingDatabase[left.number].equals(userName)) {  // 이름이 같으면
                                    bookingDatabase[left.number] = "";    // 이름 지우고
                                    userCnt--;    // 유저 예약횟수 감소
                                } else {
                                    ;   // 아무것도 안함
                                }
                            }

                            dataBaseBroker.saveBookingDatabase(context, userGroup, bookingDatabase);

                        }
                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    outerLayout.addView(textViewLeft, layoutParams);
                    textViews[textViewLeft.number] = textViewLeft;
                }
            } else {   // 마지막 행은 남은 view 개수 만큼 뽑아줘야하므로 maxSlots-(row-1)*colIndex 만큼 출력
                for (int j = 0; j < maxSlots - (row - 1) * colIndex; j++) {
                    MyTextView textViewLeft = new MyTextView(context); // textViews[i*colIndex+j];
                    //textViewLeft.setBackgroundDrawable(getResources().getDrawable(R.drawable.xml_border));
                    textViewLeft.number = i * colIndex + j;
                    textViewLeft.setText(timeArray[textViewLeft.number-1].timeToString());
                    textViewLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 현재 textView가 비어있으면 로그인 된 이름을 저장하고,
                            // 비어있지 않은 경우 로그인 된 이름과 동일하면 칸을 비우고, 로그인 된 이름과 다르면 아무런 동작을 하지 않는다.
                            MyTextView left = (MyTextView) v;

                            if (bookingDatabase[left.number].equals("")) {    // 비어있으면
                                checkCount(userName);
                                if (userCnt < settingsDatabase.maxTotalBookingSlots) {    // 예약이 가능하면
                                    if (checkCanReservation(left.number, userName)) {  // 연속 3회가 아니면 예약 가능
                                        bookingDatabase[left.number] = userName;    // 이름 넣고
                                        userCnt++;    // 유저 예약횟수 증가
                                    } else {
                                        Message.information(context, "maxTotalBookingSlots", "연속 3회 예약 불가능 합니다.");
                                    }
                                } else {   // 더 이상 예약이 불가능 하면
                                    Message.information(context, "maxTotalBookingSlots", "하루에 4회 초과예약 불가능 합니다.");
                                }

                            } else {   // 비어있지 않으면
                                checkCount(userName);
                                if (bookingDatabase[left.number].equals(userName)) {  // 이름이 같으면
                                    bookingDatabase[left.number] = "";    // 이름 지우고
                                    userCnt--;    // 유저 예약횟수 감소
                                } else {
                                    ;   // 아무것도 안함
                                }
                            }

                            dataBaseBroker.saveBookingDatabase(context, userGroup, bookingDatabase);

                        }
                    });
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,1);
                    outerLayout.setWeightSum(colIndex);
                    outerLayout.addView(textViewLeft, layoutParams);
                    textViews[textViewLeft.number] = textViewLeft;
                }
            }
        }
    }

    // drawBase() 끝
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // onDestroy() : 기존 슬롯 지우는 함수
    protected void onDestroy() {
        super.onDestroy();
        bookingDrawer.isRun = false;
        bookingDrawer.interrupt();
        try {
            bookingDrawer.join();
        } catch (InterruptedException e) {

        }
        //Log.i("jmlee", "I am die");
    }
    // onDestroy() 끝
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 터치불가 지정 메소드
    private void setUseableTextView(TextView tv, boolean usable) {
        tv.setClickable(usable);
        tv.setEnabled(usable);
        tv.setFocusable(usable);
        tv.setFocusableInTouchMode(usable);
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // MyTextView 클래스
    class MyTextView extends AppCompatTextView {
        int number;

        public MyTextView(Context context) {
            super(context);
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 시간 출력을 위한 시간배열 timeArray를 초기화하는 메소드
    private void setTimeArray() {
        timeArray = new MyTime[maxSlots];
        for (int i = 0; i < maxSlots; i++) {
            int hour = (i / 2);
            int min = (i % 2) * 30;
            timeArray[i] = new MyTime(hour, min);    //ex. 00:00, 00:30, 01:00, ...
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 시간 저장을 위한 클래스
    class MyTime {
        public int hour;
        public int min;
        public int compareTime; // 추후 시간 비교를 위한 멤버 변수

        public MyTime(int hour, int min) {
            this.hour = hour;
            this.min = min;
            compareTime = hour * 100 + min;   // ex. 01:30 는 130 으로 저장
        }

        public String timeToString() {
            return String.format("%02d:%02d", hour, min);
        }
    }
    //----------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------
    // 예약 가능여부 체크 메소드 : 가능하면 true, 불가능하면 false return
    private boolean checkCanReservation(int number, String name) {
        if(number==maxSlots-1){ // number가 49, 즉 마지막 칸일 경우에는 [앞칸,앞앞칸]검사만 검사한다.
            if (bookingDatabase[number - 2].equals(name) && bookingDatabase[number - 1].equals(name)) {
                // 불가능
                return false;
            } else {
                //가능
                return true;
            }
        }
        else if(number==maxSlots-2){  // number가 48, 즉 마지막 바로 앞칸이면 [앞칸,앞앞칸]검사와 [앞칸,뒷칸]검사를 한다.
            if (bookingDatabase[number - 2].equals(name) && bookingDatabase[number - 1].equals(name)) {
                // 불가능
                return false;
            } else if (bookingDatabase[number - 1].equals(name) && bookingDatabase[number + 1].equals(name)) {
                // 불가능
                return false;
            } else {
                //가능
                return true;
            }
        }
        else{   // number가 그 이외의 숫자면 [앞앞칸,앞칸]검사와 [앞칸,뒷칸]검사와 [뒷칸,뒷뒷칸]검사 모두 진행한다.
            if (bookingDatabase[number - 2].equals(name) && bookingDatabase[number - 1].equals(name)) {
                // 불가능
                return false;
            } else if (bookingDatabase[number - 1].equals(name) && bookingDatabase[number + 1].equals(name)) {
                // 불가능
                return false;
            } else if (bookingDatabase[number + 1].equals(name) && bookingDatabase[number + 2].equals(name)) {
                // 불가능
                return false;
            } else {
                //가능
                return true;
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------
    // 현재 로그인한 아이디로 예약된 횟수 체크하는 메소드
    private void checkCount(String name) {
        int cnt = 0;
        for (int i = 0; i < maxSlots; i++) {
            if (bookingDatabase[i].equals(name)) {
                cnt++;
            }
            userCnt = cnt;
        }
    }
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
                BookingAhhActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("취소", null);  // 다이얼로그 취소버튼은 처리하지않음.
        builder.create().show();
    }
    //----------------------------------------------------------------------------------------------------------------------------
}
