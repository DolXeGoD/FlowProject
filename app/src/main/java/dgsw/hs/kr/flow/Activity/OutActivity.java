package dgsw.hs.kr.flow.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;

import dgsw.hs.kr.flow.Database.DBManager;
import dgsw.hs.kr.flow.Model.request.Out;
import dgsw.hs.kr.flow.Model.response.ResponseFormat;
import dgsw.hs.kr.flow.Network.RetrofitService;
import dgsw.hs.kr.flow.R; //R 오류가 왜 뜨는지 모르겠네 ㅠㅠ 돌려막기
import dgsw.hs.kr.flow.Utils.APIUtills;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutActivity extends AppCompatActivity {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String S_DATE;
    private String S_TIME;
    private String S_DATETIME;
    private String E_DATE;
    private String E_TIME;
    private String E_DATETIME;
    private String REASON_TO_OUT;
    private String USER_TOKEN;
    private static final String TAG = "OutActivity";
    private boolean isUserSleep; // true = sleep, false = go
    private RetrofitService mRTService;
    private Call<ResponseFormat> mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out);

        Button startdate_btn = findViewById(R.id.btn_setStartDate);
        Button enddate_btn = findViewById(R.id.btn_setEndDate);
        Button starttime_btn = findViewById(R.id.btn_setStartTime);
        Button endtime_btn = findViewById(R.id.btn_setEndTime);
        Button submit_btn = findViewById(R.id.btn_outSubmit);
        final CheckBox isGo = findViewById(R.id.isGoOut);
        final CheckBox isSleep = findViewById(R.id.isSleepOut);
        final EditText reason_et = findViewById(R.id.et_reason);
        final DBManager dbManager = new DBManager(getApplicationContext(), "FlowUser.db", null, 1);
        //유저 인증 토큰
        USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyRW1haWwiOiIwMDB3aGd1c3dvQGRnc3cuaHMua3IiLCJjbGFzc0lkeCI6MiwiYXV0aCI6MiwiaWF0IjoxNTMwMjUwNDExLCJleHAiOjE1MzA4NTUyMTEsImlzcyI6ImplZmZjaG9pLmNvbSIsInN1YiI6InRva2VuIn0.WV94J6aX4AVfunMlwHYcqJjslqx6y_C19W8y8dtMaFw";

        final Calendar cal = Calendar.getInstance();
        final DatePickerDialog S_Ddialog = new DatePickerDialog(this, sdateListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        final TimePickerDialog S_Tdialog = new TimePickerDialog(this, stimeListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        final DatePickerDialog E_Ddialog = new DatePickerDialog(this, edateListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        final TimePickerDialog E_Tdialog = new TimePickerDialog(this, etimeListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);

        //================================= OnClick Listener About DATE & TIME
        startdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                S_Ddialog.show();
            }
        });

        starttime_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(S_DATE == null){
                    Toast.makeText(getApplicationContext(), "시간을 설정하기 전에 날짜부터 설정해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    S_Tdialog.show();
                }
            }
        });

        enddate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(S_DATETIME == null){
                    Toast.makeText(getApplicationContext(), "시작 날짜 및 시간부터 선택해주세요.", Toast.LENGTH_SHORT).show();
                } else{
                    E_Ddialog.show();
                }
            }
        });

        endtime_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(E_DATE == null){
                    Toast.makeText(getApplicationContext(), "시간을 설정하기 전에 날짜부터 설정해주세요.", Toast.LENGTH_SHORT).show();
                } else{
                    E_Tdialog.show();
                }
            }
        });

        //==================== SUBMIT ONCLICK LISTENER
        submit_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(), "버튼 눌림.", Toast.LENGTH_SHORT).show();

                REASON_TO_OUT = reason_et.getText().toString().trim();
                final Out out = new Out();

                //모델 세팅 전 값 비었는지 검사.
                if(S_DATETIME!=null&&E_DATETIME!=null&&!TextUtils.isEmpty(REASON_TO_OUT)){
                    //SimpleDateFormat

                    //외출인지 외박인지 확인.
                    if(isGo.isChecked() == true){
                        isUserSleep = false;
                    } else if(isSleep.isChecked() == true){
                        isUserSleep = true;
                    }

                    out.setStart_time(S_DATETIME);
                    out.setEnd_time(E_DATETIME);
                    out.setReason(REASON_TO_OUT);

                    Log.i(TAG, "request msg : " + S_DATETIME);
                    Log.i(TAG, "request msg : " + E_DATETIME);
                    Log.i(TAG, "request msg : " + REASON_TO_OUT);
                    Toast.makeText(getApplicationContext(), "레트로핏 시작.", Toast.LENGTH_SHORT).show();

                    mRTService = APIUtills.getAPIService(); //start of retrofit
                    if(isUserSleep == false){
                        mResponse = mRTService.goOutPost(out, USER_TOKEN);
                        mResponse.enqueue(new Callback<ResponseFormat>() {
                            @Override
                            public void onResponse(Call<ResponseFormat> call, Response<ResponseFormat> response) {
                                // 네트워크 요청이 성공했을 시. (code number is 200~300)
                                if(response.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "외출 신청입니다.", Toast.LENGTH_LONG).show(); //DEBUG
                                    Log.i(TAG, "response msg : " + response.message().toString());
                                    Log.i(TAG, "response code : " + response.code());
                                    // 서버로 요청 성공했을 시. success to get body status
                                    if(response.body() != null){
                                        Log.i(TAG, "server msg : " + response.body().getMessage());
                                        Log.i(TAG, "server code : " + response.body().getStatus());
                                        //서버가 정상적이고, 요청도 성공하여 body의 data를 성공적으로 response 받았을 때. body status is 200
                                        if(response.body().getResponseFormatData() != null && response.body().getStatus() == 200) {
                                            Toast.makeText(getApplicationContext(),"외출 신청 성공.",Toast.LENGTH_LONG).show();
                                            Log.i(TAG, "re-check ur goout starttime : " + response.body().getResponseFormatData().getGo_out().getStart_time());
                                            Log.i(TAG, "re-check ur goout endtime : " + response.body().getResponseFormatData().getGo_out().getEnd_time());

                                            //외출 신청 기록을 DB에 저장
                                            int accept = 0;
                                            String start_time = response.body().getResponseFormatData().getGo_out().getStart_time();
                                            String end_time =  response.body().getResponseFormatData().getGo_out().getEnd_time();
                                            String reason = response.body().getResponseFormatData().getGo_out().getReason();
                                            int class_idx = response.body().getResponseFormatData().getGo_out().getClass_idx();
                                            String student_email = response.body().getResponseFormatData().getGo_out().getStudent_email();
                                            int server_idx = response.body().getResponseFormatData().getGo_out().getIdx();
                                            dbManager.out_insert(accept, start_time, end_time, reason, class_idx, student_email, server_idx);
                                        } else{
                                            Log.i(TAG, "server is well response, but failed to get data.. Check the error code. ");
                                        }
                                    } else{
                                        Log.e(TAG, "Failed to get response body. SERVER ERR");
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<ResponseFormat> call, Throwable t) {
                                Log.e(TAG, "외박 신청 실패 - 레트로핏 onFailure 걸림.");
                                Log.e(TAG, "GoOutResponseFormat Request Failed");
                            }
                        });
                    }
                    else if(isUserSleep == true){
                        mResponse = mRTService.sleepOutPost(out, USER_TOKEN);
                        mResponse.enqueue(new Callback<ResponseFormat>() {
                            @Override
                            public void onResponse(Call<ResponseFormat> call, Response<ResponseFormat> response) {
                                // 네트워크 요청이 성공했을 시. (code number is 200~300)
                                if(response.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "외박 신청입니다.", Toast.LENGTH_LONG).show(); //DEBUG
                                    Log.i(TAG, "response msg : " + response.message().toString());
                                    Log.i(TAG, "response code : " + response.code());
                                    // 서버로 요청 성공했을 시. success to get body status
                                    if(response.body() != null){
                                        Log.i(TAG, "server msg : " + response.body().getMessage());
                                        Log.i(TAG, "server code : " + response.body().getStatus());
                                        //서버가 정상적이고, 요청도 성공하여 body의 data를 성공적으로 response 받았을 때. body status is 200
                                        if(response.body().getResponseFormatData() != null && response.body().getStatus() == 200) {
                                            Toast.makeText(getApplicationContext(),"외박 신청 성공.",Toast.LENGTH_LONG).show();
                                            Log.i(TAG, "re-check ur goout starttime : " + response.body().getResponseFormatData().getSleep_out().getStart_time());
                                            Log.i(TAG, "re-check ur goout endtime : " + response.body().getResponseFormatData().getSleep_out().getEnd_time());

                                            //외박 신청 기록을 DB에 저장
                                            String start_time = response.body().getResponseFormatData().getSleep_out().getStart_time();
                                            String end_time =  response.body().getResponseFormatData().getSleep_out().getEnd_time();
                                            String reason = response.body().getResponseFormatData().getSleep_out().getReason();
                                            int class_idx = response.body().getResponseFormatData().getSleep_out().getClass_idx();
                                            String student_email = response.body().getResponseFormatData().getSleep_out().getStudent_email();
                                            int server_idx = response.body().getResponseFormatData().getSleep_out().getIdx();
                                            dbManager.out_insert(0, start_time, end_time, reason, class_idx, student_email, server_idx);
                                        } else{
                                            Log.i(TAG, "server is well response, but failed to get data.. Check the error code. ");
                                        }
                                    } else{
                                        Log.e(TAG, "Failed to get response body. SERVER ERR");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseFormat> call, Throwable t) {
                                Log.e(TAG, "외박 신청 실패 - 레트로핏 onFailure 걸림.");
                                Log.e(TAG, "GoOutResponseFormat Request Failed");
                            }
                        });
                    }
                }
            }
        });
    }


    //=========================================== onDataSet Listener
    private DatePickerDialog.OnDateSetListener sdateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int thisYear, int monthOfYear, int dayOfMonth) {
            year = thisYear;
            month = monthOfYear + 1;
            day = dayOfMonth;
            Toast.makeText(getApplicationContext(), year +
                    "년" + month + "월" + day+"일", Toast.LENGTH_SHORT).show();

            S_DATE = String.format("%04d-%02d-%02d", year, month , day);
        }
    };

    private TimePickerDialog.OnTimeSetListener stimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int h, int m) {
            hour = h;
            minute = m;
            Toast.makeText(getApplicationContext(), hour + " 시" + minute +
                    " 분 입니다.", Toast.LENGTH_SHORT).show();
            S_TIME = String.format("%02d:%02d", hour, minute);
            S_DATETIME = S_DATE+" "+S_TIME;
            System.out.println(S_DATETIME);
        }
    };

    private DatePickerDialog.OnDateSetListener edateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int thisYear, int monthOfYear, int dayOfMonth) {
            year = thisYear;
            month = monthOfYear + 1;
            day = dayOfMonth;
            Toast.makeText(getApplicationContext(), year +
                    "년" + month + "월" + day+"일", Toast.LENGTH_SHORT).show();
            E_DATE = String.format("%04d-%02d-%02d", year, month , day);
        }
    };

    private TimePickerDialog.OnTimeSetListener etimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int h, int m) {
            hour = h;
            minute = m;
            Toast.makeText(getApplicationContext(), hour + " 시" + minute +
                    " 분 입니다.", Toast.LENGTH_SHORT).show();
            E_TIME = String.format("%02d:%02d", hour, minute);
            E_DATETIME = E_DATE+" "+E_TIME;
        }
    };

}
