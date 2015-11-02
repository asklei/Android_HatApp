package edu.msu.xulei1.leixumadhatter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class HatterActivity extends AppCompatActivity {
    public final static String COLOR_MESSAGE = "edu.msu.xulei1.leixumadhatter.HAT_COLOR";
    /**
     * Request code when selecting a picture
     */
    private static final int SELECT_PICTURE = 1;
    /**
     * Request code when selecting a color
     */
    private static final int SELECT_COLOR = 2;

    private static final String PARAMETERS = "parameters";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getHatterView().putToBundle(PARAMETERS, outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hatter);

        /*
         * Set up the spinner
         */
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hats_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        getSpinner().setAdapter(adapter);

        getSpinner().setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int pos, long id) {
                getHatterView().setHat(pos);
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        /*
         * Restore any state
         */
        if(savedInstanceState != null) {
            getHatterView().getFromBundle(PARAMETERS, savedInstanceState);
            getSpinner().setSelection(getHatterView().getHat());
            updateUI();
        }
    }

    /**
     * Handle a Picture button press
     * @param view view
     */
    public void onPicture(View view) {
        // Get a picture from the gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Handle a color button press
     * @param  view view
     */
    public void onColor(View view) {
        Intent intent = new Intent(this,ColorSelectActivity.class);
        startActivityForResult(intent, SELECT_COLOR);
    }

    /**
     * Handle feather checkbox
     */
    public void onFeather(View view) {
        getHatterView().setFeather(getFeatherCheck().isChecked());
    }

    /**
     * Ensure the user interface components match the current state
     */
    private void updateUI() {
        getColorButton().setEnabled(getHatterView().getHat() == HatterView.HAT_CUSTOM);
    }

    /**
     * Function called when we get a result from some external
     * activity called with startActivityForResult()
     * @param requestCode the request code we sent to the activity
     * @param resultCode a result of from the activity - ok or cancelled
     * @param data data from the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            // Response from the picture selection activity
            Uri imageUri = data.getData();

            // We have to query the database to determine the document ID for the image
            Cursor cursor = getContentResolver().query(imageUri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":")+1);
            cursor.close();

            // Next, we query the content provider to find the path for this
            // document id.
            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            if(path != null) {
                Log.i("Path", path);
                getHatterView().setImagePath(path);
            }

        } else if (requestCode == SELECT_COLOR && resultCode == Activity.RESULT_OK) {
            int color = data.getIntExtra(COLOR_MESSAGE, Color.CYAN);
            getHatterView().setColor(color);
        }
    }

    /**
     * The hatter view object
     */
    private HatterView getHatterView() {
        return (HatterView) findViewById(R.id.hatterView);
    }

    /**
     * The color select button
     */
    private Button getColorButton() {
        return (Button)findViewById(R.id.buttonColor);
    }

    /**
     * The feather checkbox
     */
    private CheckBox getFeatherCheck() {
        return (CheckBox)findViewById(R.id.checkFeather);
    }

    /**
     * The hat choice spinner
     */
    private Spinner getSpinner() {
        return (Spinner) findViewById(R.id.spinnerHat);
    }
}
