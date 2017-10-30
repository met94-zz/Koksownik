/* 
 * Copyright (C) 2017 Alan Bara, alanbarasoft@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package zls.mutek.koksownik;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Tree<String> tree;
    private String tree_filename;

    private final static String TAG = "MAIN_TAG";
    private final class Permissions {
        private final static String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    }
    public TextView pathTextView;
    public ImageButton playButton;
    public ImageButton stopButton;
    public Chronometer chronometer;
    public boolean playButtonPressed=false;
    public boolean stopButtonPressed=true;
    public long timeCounted=0;
    public boolean saveLogs=false;
    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    enum Themes {
        Dark,
        Light
    }

    static Themes activeTheme = Themes.Dark;
    static final String ACTIVE_THEME_BUNDLE = "activeTheme_BUNDLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initializing settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsActivity.initializeResources(this);
        mSettingsListener = getOnSharedPreferenceChange();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSettingsListener);
        mSettingsListener.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(this), SettingsActivity.KEY_THEME); //set activeTheme
        saveLogs =  PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_SAVE_LOGS, false);

        if(savedInstanceState != null) {
            activeTheme = (Themes)savedInstanceState.get(ACTIVE_THEME_BUNDLE);
        }

        if(activeTheme == Themes.Light) {
            setTheme(R.style.AppThemeLight_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        if(activeTheme == Themes.Dark) {
            ContextCompat.getDrawable(this, R.drawable.ic_save_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
            ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
            ContextCompat.getDrawable(this, R.drawable.ic_history_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
        } else if(activeTheme == Themes.Light) {
            ContextCompat.getDrawable(this, R.drawable.ic_save_black_48dp).clearColorFilter();
            ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).clearColorFilter();
            ContextCompat.getDrawable(this, R.drawable.ic_history_black_48dp).clearColorFilter();
        }

        super.onCreate(savedInstanceState);

        if(!Utils.askPermissions(this, new String[] { Permissions.READ_PHONE_STATE })) {
            return;
        }

        //EULA

        if(!Utils.checkPreferencesSecurityKey(this))
        {
            InputSecurityKeyDialogFragment inputSecurityKeyDialogFragment = new InputSecurityKeyDialogFragment();
            inputSecurityKeyDialogFragment.show(getFragmentManager(), getString(R.string.dialog_key_tag));
        }

        setContentView(R.layout.activity_main);

        pathTextView = (TextView)findViewById(R.id.path_textView);

        setTreeFilename(getString(R.string.tree_filename));

        if(getSupportActionBar() == null) {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.actionbar_main);
            setSupportActionBar(myToolbar);
            if(activeTheme == Themes.Light) {
                myToolbar.getContext().setTheme(R.style.AppThemeLight_NoActionBar);
                myToolbar.setTitleTextColor(0xde000000);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    myToolbar.setBackgroundColor(0x20444444);
                }
            }
        }

        File file = getFileStreamPath(getTreeFilename());
        if(!file.exists())
        {
            try {
                XMLUtils.getInstance(this).createInitialXML(getTreeFilename());
            } catch (IOException e) {
                Utils.showAlertDialog(this, R.string.error_occurred, R.string.file_unknown_error);
                Log.w(TAG, getString(R.string.file_unknown_error));
                Log.w(TAG, e.toString());
            }
        }
        try {
            InputStream in = openFileInput(getTreeFilename());
            tree = XMLUtils.getInstance(this).parseXmlToTree(/*tree*/null, in);
        } catch(FileNotFoundException e) {
            Utils.showAlertDialog(this, R.string.error_occurred, R.string.file_creation_failure);
            Log.w(TAG, getString(R.string.file_creation_failure));
            Log.w(TAG, e.toString());
        } catch (XmlPullParserException e) {
            Utils.showAlertDialog(this, R.string.error_occurred, R.string.file_corrupted);
            Log.w(TAG, getString(R.string.file_corrupted));
            Log.w(TAG, e.toString());
        } catch (IOException e) {
            Utils.showAlertDialog(this, R.string.error_occurred, R.string.file_unknown_error);
            Log.w(TAG, getString(R.string.file_unknown_error));
            Log.w(TAG, e.toString());
        } finally {
            if(tree == null) {
                Utils.showAlertDialog(this, R.string.failure, R.string.general_failure);
                tree = new Tree<String>("root", null);
            }
        }
        MainListFragment fr = new MainListFragment();
        Bundle args = new Bundle(1);
        args.putString("path", "root");
        fr.setArguments(args);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fr).commit();

        chronometer = (Chronometer)findViewById(R.id.timerChronometer);
        playButton = (ImageButton)findViewById(R.id.timer_play_Button);
        playButton.setOnClickListener(this);
        stopButton = (ImageButton)findViewById(R.id.timer_stop_Button);
        stopButton.setOnClickListener(this);

    }

    /**********************************
     *
     * On Request Permission Result
     *
     **********************************/
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults)
    {
        if(requestCode == Utils.REQUEST_CODE_ASK_PERMISSIONS)
        {
            for(int i=0; i<permissions.length && i<grantResults.length; i++)
            {
                if(permissions[i].compareTo(Permissions.READ_PHONE_STATE) == 0) {
                    if(grantResults[i] == PERMISSION_DENIED) {
                        Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.dialog_noperms);
                        dialog.setTitle(R.string.dialog_missing_permsTitle);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        recreate();
                    }
                }
            }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        try {
            if(getTreeFilename() != null) {
                XMLUtils.getInstance(this).validXmlWithTree(this, getTree(), getTreeFilename());
                XMLUtils.getInstance(this).saveValidatedTreeToXML(getTree(), getTreeFilename());
            }
        } catch(Exception e) {
            Log.w(TAG, getString(R.string.file_unknown_error));
            Log.w(TAG, e.toString());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (saveLogs) {
                Utils.saveLogcatToFile(this);
            }
        }
    }


    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState) {

        savedInstanceState.putSerializable(ACTIVE_THEME_BUNDLE, activeTheme);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId()) {
            case R.id.timer_play_Button:
                if(!playButtonPressed) {
                    playButton.setImageResource(R.drawable.ic_pause_black_48dp);
                    if(stopButtonPressed) {
                        chronometer.setBase(SystemClock.elapsedRealtime());
                    } else {
                        chronometer.setBase(SystemClock.elapsedRealtime() - (timeCounted - chronometer.getBase()));
                    }
                    chronometer.start();
                    stopButtonPressed = false;
                } else {
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                    timeCounted = SystemClock.elapsedRealtime();
                    chronometer.stop();
                }
                playButtonPressed = !playButtonPressed;
                break;
            case R.id.timer_stop_Button:
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                if(playButtonPressed) {
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                }
                stopButtonPressed = true;
                playButtonPressed = false;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.actionbar_save:
                try {
                    if(getTreeFilename() != null) {
                        XMLUtils.getInstance(this).validXmlWithTree(this, getTree(), getTreeFilename());
                        XMLUtils.getInstance(this).saveValidatedTreeToXML(getTree(), getTreeFilename());
                        Toast.makeText(this, R.string.save_succes, Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    Utils.showAlertDialog(this, R.string.error_occurred, R.string.file_unknown_error);
                    Log.w(TAG, getString(R.string.file_unknown_error));
                    Log.w(TAG, e.toString());
                } finally {
                    if(saveLogs) {
                        Utils.saveLogcatToFile(this);
                    }
                }
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*****
     * getters and setters section
     */
    public Tree<String> getTree()
    {
        return tree;
    }
    public String getTreeFilename()
    {
        return tree_filename;
    }
    public void setTreeFilename(String name)
    {
        tree_filename = name;
    }

    /**********************************
     *
     * On Preference Changed
     *
     **********************************/

    private SharedPreferences.OnSharedPreferenceChangeListener getOnSharedPreferenceChange() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(SettingsActivity.KEY_THEME)) {
                    if(sharedPreferences.getBoolean(key, true)) {
                        MainActivity.activeTheme = Themes.Dark;
                    } else {
                        MainActivity.activeTheme = Themes.Light;
                    }
                    //MainActivity.this.getTheme().applyStyle(R.style.AppTheme, true);
                } else if(key.equals(SettingsActivity.KEY_SAVE_LOGS)) {
                    saveLogs = sharedPreferences.getBoolean(key, false);
                }
            }
        };
    }
}
