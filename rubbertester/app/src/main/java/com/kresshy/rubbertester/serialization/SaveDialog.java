package com.kresshy.rubbertester.serialization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kresshy.rubbertester.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveDialog extends Activity {

    Button save;
    Button cancel;
    TextView location;
    EditText fileName;

    SerializableMeasurementStorage measurementStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_dialog);

        fileName = (EditText) findViewById(R.id.saveValue);
        location = (TextView) findViewById(R.id.saveLocation);
        save = (Button) findViewById(R.id.btnsaveok);
        cancel = (Button) findViewById(R.id.btnsavecancel);


        String fileNameTimeStamp = new SimpleDateFormat(
                "HH-mm_dd-MM-yyyy"
        ).format(new Date());

        fileName.setText(fileNameTimeStamp);

        measurementStorage = (SerializableMeasurementStorage) getIntent().getSerializableExtra("FileToSave");

        save.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String name = fileName.getText().toString();

                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + "/RubberTesterFiles";

                if (name == null || name.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please write in the file name!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    File dir = new File(path);
                    dir.mkdir();

                    try {
                        FileOutputStream fos = new FileOutputStream(path + "/"
                                + name + ".rtm");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(measurementStorage);

                        oos.close();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(getPackageName(), name);
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

                Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
            }
        });

    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // getMenuInflater().inflate(R.menu.activity_save_dialog, menu);
    // return true;
    // }
}
