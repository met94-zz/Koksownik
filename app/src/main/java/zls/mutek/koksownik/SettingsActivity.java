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


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by abara on 5/9/2017.
 * settings activity class
 */

public class SettingsActivity extends Activity {
    public static String KEY_SAVE_LOGS;
    public static String KEY_THEME;

    static final String TAG = "SA_TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (MainActivity.activeTheme == MainActivity.Themes.Light) {
            setTheme(R.style.AppThemeLight_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), getString(R.string.preferencesFragmentTag))
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        boolean result = true;
        if (requestCode == ButtonPreference.PICK_DIR_CREATE_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                DocumentFile df = DocumentFile.fromTreeUri(this, uri);
                if(df == null && resultData.hasExtra("file")) {
                    df = DocumentFile.fromFile((File)resultData.getSerializableExtra("file"));
                }
                if(df != null) {
                    for (File file : getFilesDir().listFiles()) {
                        try {
                            if(file.isFile()) {
                                String fileName = file.getName();
                                int idx = fileName.lastIndexOf(".xml");
                                if (idx != -1) {
                                    fileName = fileName.substring(0, idx); //strip extension
                                }
                                DocumentFile newFile = df.createFile("text/xml", fileName);
                                if (newFile != null) {
                                    Utils.copyFile(new FileInputStream(file), getContentResolver().openOutputStream(newFile.getUri()));
                                }
                            }
                        } catch (IOException e) {
                            Log.w(TAG, e.toString());
                            Toast.makeText(this, R.string.backup_fail + file.getName(), Toast.LENGTH_SHORT).show();
                            result = false;
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    result = false;
                }
                if(result) {
                    Toast.makeText(this, R.string.backup_success, Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == ButtonPreference.PICK_DIR_IMPORT_BACKUP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                DocumentFile df = DocumentFile.fromTreeUri(this, uri); //copy from
                if(df == null && resultData.hasExtra("file")) {
                    df = DocumentFile.fromFile((File)resultData.getSerializableExtra("file"));
                }
                if(df != null) {
                    for (DocumentFile file : df.listFiles()) {
                        try {
                            File newFile = new File(getFilesDir(), file.getName());
                            Utils.copyFile(getContentResolver().openInputStream(file.getUri()), new FileOutputStream(newFile));
                        } catch (IOException e) {
                            Log.w(TAG, e.toString());
                            Toast.makeText(this, R.string.backup_fail + file.getName(), Toast.LENGTH_SHORT).show();
                            result = false;
                        }
                    }
                    Utils.restartApplication(this);
                } else {
                    Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    result = false;
                }
                if(result) {
                    Toast.makeText(this, R.string.import_backup_success, Toast.LENGTH_SHORT).show();
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, resultData);
    }


    public static void initializeResources(MainActivity activity) {
        KEY_THEME = activity.getString(R.string.generalPreferences_themeKey);
        KEY_SAVE_LOGS = activity.getString(R.string.generalPreferences_createLogsKey);

        //activity.MsgHeader =  PreferenceManager.getDefaultSharedPreferences(activity).getString(SettingsActivity.KEY_MSG_HEADER, SettingsActivity.MSG_HEADER_DEF);
        //activity.useDefaultApp =  PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(SettingsActivity.KEY_DEFAULTAPP, false);
    }
}
