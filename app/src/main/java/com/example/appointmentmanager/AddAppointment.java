package com.example.appointmentmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.PointerIcon;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddAppointment extends AppCompatActivity {

    //refrences from view classes
    EditText title, note;
    TextView time;
    MaterialCalendarView calenderForDates;
    Spinner colors;
    ImageView imageForColor;


    //refrences from classes
    Appointment newAppointment;
    Calendar calendar;
    MaterialTimePicker picker;
    FragmentManager fragmentManager;
    DateFormat dateFormat;
    AlertDialog.Builder dlgAlert;
    Thread TimePickerThead;
    DateTimeFormatter formatter;
    LocalDate OpenDate;
    MyPrefernce Prefrence;

    //variables
    int year;
    int month;
    int day;
    String dateString,CurrentDate, selectedColor, recievedDate,time_forDatabase;
    Date selectedTime;
    //datastructure
    List<String> AllSelecteDates;
    private LocalTime date_forDatabase;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);



        recievedDate=getIntent().getStringExtra("date");

        //find in activity
        title = findViewById(R.id.tx_title);
        note = findViewById(R.id.tx_notes);
        time = findViewById(R.id.TX_dateFromButton);
        calenderForDates = findViewById(R.id.Cl_calender);
        colors = findViewById(R.id.spinner_colors);
        imageForColor = findViewById(R.id.imageView_color);


        // take objects
        newAppointment = new Appointment();
        calendar = Calendar.getInstance();
        dateFormat = new DateFormat();
        AllSelecteDates = new ArrayList<>();
        dlgAlert = new AlertDialog.Builder(this);
        colors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    imageForColor.setColorFilter(Color.BLACK);
                    selectedColor = "black";
                } else if (position == 1){
                    imageForColor.setColorFilter(Color.BLUE);
                    selectedColor = "blue";
                }
                else if (position == 2){
                    imageForColor.setColorFilter(Color.RED);
                    selectedColor = "red";
                }
                else if (position == 3) {
                    imageForColor.setColorFilter(Color.YELLOW);
                    selectedColor = "yellow";
                }
                    else{
                    imageForColor.setColorFilter(Color.WHITE);
                    selectedColor = "white";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //events
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);

//fragment for time picker
        fragmentManager = getSupportFragmentManager();

        //set callender
        formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        OpenDate = LocalDate.now();
        if ( getIntent().getStringExtra("date")!= null) {
            OpenDate = LocalDate.parse(recievedDate,formatter);
        }
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calenderForDates.state().edit().setMinimumDate(LocalDate.now()).commit();
        calenderForDates.setSelectedDate(OpenDate);


//add date which will open on
        formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        AllSelecteDates.add(OpenDate.toString());


        calenderForDates.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                dateString = date.getDate().toString();
                if (AllSelecteDates.contains(dateString)) {
                    AllSelecteDates.remove(AllSelecteDates.indexOf(dateString));

                } else {
                    AllSelecteDates.add(dateString);
                }
            }
        });


        imageForColor.setColorFilter(Color.RED);

        //notifications


    }

    //methods
    public void insert(View view) {
        if (AllSelecteDates.size() > 0 && !title.getText().toString().isEmpty() && !time.getText().toString().isEmpty()) {
            for (int i = 0; i < AllSelecteDates.size(); i++) {
                date_forDatabase = LocalTime.of(selectedTime.getHours(), selectedTime.getMinutes());
                time_forDatabase = date_forDatabase.getHour()+":"+date_forDatabase.getMinute();
                 formatter = DateTimeFormatter.ISO_LOCAL_TIME;
                newAppointment.color = selectedColor;
                newAppointment.note = note.getText().toString();
                newAppointment.title = title.getText().toString();
                newAppointment.date_time = AllSelecteDates.get(i) + "T" + date_forDatabase.format(formatter);
                AppointmentManagerDatabase.getInstance(this).appointmentDAO2().insertAppointment(newAppointment);
                Toast.makeText(this, "Appointment Saved", Toast.LENGTH_SHORT).show();

                 CurrentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                 Prefrence = MyPrefernce.getInstance(this);

                 {
                    if (AllSelecteDates.get(i).equals(CurrentDate)) {
                        Set<String> modifyToDaydates = new HashSet<>();
                        modifyToDaydates=   Prefrence.getSetStringData("todayDates");

                        List<String> Tempdates = AppointmentManagerDatabase.getInstance(AddAppointment.this).appointmentDAO2().GetToDayAppointment("%" + CurrentDate + "%");
                        for (int i2 = 0; i2 < Tempdates.size(); i2++) {
                            modifyToDaydates.add(Tempdates.get(i2).replace(CurrentDate+"T", ""));
                        }

                        Prefrence.storeSetData("todayDates",modifyToDaydates);
                    }
                }
            }
            onBackPressed();
        } else Toast.makeText(this, "Please enter a valid data", Toast.LENGTH_LONG).show();
    }


    public void timrDialog(View view) {
//        timePicker.show();
        TimePickerThead = new Thread(new Runnable() {
            @Override
            public void run() {
                picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(11).setMinute(0).setTitleText("Select Appointment time").setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD).build();
                picker.show(fragmentManager, "tag");
                picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


//               calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
                        calendar.set(Calendar.MINUTE, picker.getMinute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        selectedTime = calendar.getTime();
                        dateString = dateFormat.format("hh.mm aa", selectedTime).toString();
                        time.setText(dateString);
                    }
                });


            }
        });
        TimePickerThead.start();


    }



    public void back(View view) {
        dlgAlert.setMessage("ARE YOU SURE TO DISCARD THIS APPOINTMENT");
        dlgAlert.setTitle("DISCARD");
//        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                        onBackPressed();
                    }
                });
        dlgAlert.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dlgAlert.create().show();

        //
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void test(View view) {
       PointerIcon m= calenderForDates.getPointerIcon();
        calenderForDates.postInvalidateOnAnimation();

        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();

    }
}