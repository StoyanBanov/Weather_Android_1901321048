package com.stoyan.weather_android_1901321048;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends DBActivity {

    protected ListView simpleList;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_history);

        linearLayout = findViewById(R.id.historyLayout);
        linearLayout.getBackground().setAlpha(85);

        simpleList = findViewById(R.id.simpleList);

        final ArrayList<String> listResults= new ArrayList<>();
        try {
            initDB();
            SelectSQL(
                    "SELECT * FROM QueryHistory ORDER BY ID DESC",
                    null,
                    (ID, Place, Type, Date)->{
                        listResults.add(ID + ". Location: " + Place + "\n" + "\t" + " Forecast: " + Type + "\n" + "\t" + " Date: " + Date + "\n");
                    }
            );
            simpleList.clearChoices();
            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(getApplicationContext(),  R.layout.activity_listview, R.id.textView, listResults);
            simpleList.setAdapter(arrayAdapter);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(HistoryActivity.this, ForecastTodayActivity.class));
        finish();
    }
}