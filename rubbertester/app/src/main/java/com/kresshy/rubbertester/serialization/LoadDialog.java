package com.kresshy.rubbertester.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kresshy.rubbertester.R;

public class LoadDialog extends Activity {
    RadioGroup rGroup;
    Button ok;
    Button cancel;
    TextView location;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_dialog);

        rGroup = (RadioGroup) findViewById(R.id.loadGroup);
        ok = (Button) findViewById(R.id.btnloadok);
        cancel = (Button) findViewById(R.id.btnloadcancel);
        location = (TextView) findViewById(R.id.storeLocation);

        String path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/RubberTesterFiles";
        File dir1 = new File(path1);
        dir1.mkdir();

        StringBuilder sb = new StringBuilder();

        File[] files = dir1.listFiles();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i].getName()).append("#");
        }

        final String[] fileNames = sb.toString().split("#");

        List<RadioButton> list = new ArrayList<RadioButton>();

        for (int i = 0; i < fileNames.length; i++) {
            list.add(new RadioButton(getApplicationContext()));
            list.get(i).setText(fileNames[i]);
            list.get(i).setLayoutParams(
                    new LayoutParams(LayoutParams.FILL_PARENT,
                            LayoutParams.WRAP_CONTENT));
            list.get(i).setPadding(10, 10, 10, 10);
            list.get(i).setTextColor(Color.DKGRAY);
            rGroup.addView(list.get(i));
        }

        ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int selectid = rGroup.getCheckedRadioButtonId();

                if (selectid == -1) {

                    Toast.makeText(getApplicationContext(),
                            "Please select a measurement!", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    RadioButton selected = (RadioButton) findViewById(selectid);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(getPackageName(), selected.getText());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        location.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + "/RubberTesterFiles";

                Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG)
                        .show();
            }
        });

    }
}