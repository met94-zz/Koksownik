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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

/**
 * Created by abara on 6/16/2017.
 */

public class ButtonPreference extends Preference {

    private Button mRefreshThemeButton;
    private Button mBackupButton;
    private Button mImportBackupButton;

    static final int PICK_DIR_CREATE_BACKUP_REQUEST_CODE=42;
    static final int PICK_DIR_IMPORT_BACKUP_REQUEST_CODE=43;

    public ButtonPreference(Context context)
    {
        super(context);
    }

    public ButtonPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mRefreshThemeButton = (Button) view.findViewById(R.id.button_refresh_theme);
        if(mRefreshThemeButton != null) {
            if (!mRefreshThemeButton.hasOnClickListeners()) {
                mRefreshThemeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                        Intent i = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(i);
                        */
                        Utils.restartApplication(getContext());
                        //getTheme().applyStyle(R.style.AppTheme, true);
                    }
                });
            }
        }

        mBackupButton = (Button) view.findViewById(R.id.button_backup);
        if(mBackupButton != null) {
            if (!mBackupButton.hasOnClickListeners()) {
                mBackupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (getContext() instanceof SettingsActivity) {
                                Intent data = new Intent();
                                File dir = new File((Utils.isExternalStorageWritable()) ? getContext().getExternalFilesDir(null) : getContext().getFilesDir(),
                                        getContext().getString(R.string.backup_dir_name));
                                if(!dir.exists()) {
                                    try {
                                        dir.mkdir();
                                    } catch(SecurityException e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if(dir.exists()) {
                                    data.putExtra("file", dir);
                                    ((SettingsActivity) getContext()).onActivityResult(PICK_DIR_CREATE_BACKUP_REQUEST_CODE, Activity.RESULT_OK, data);
                                } else {
                                    Toast.makeText(getContext(), getContext().getString(R.string.backup_dir_creating_failure), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            if (getContext() instanceof Activity) {
                                ((Activity) getContext()).startActivityForResult(intent, PICK_DIR_CREATE_BACKUP_REQUEST_CODE);
                            }
                        }
                    }
                });
            }
        }

        mImportBackupButton = (Button) view.findViewById(R.id.button_import_backup);
        if(mImportBackupButton != null) {
            if (!mImportBackupButton.hasOnClickListeners()) {
                mImportBackupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (getContext() instanceof SettingsActivity) {
                                Intent data = new Intent();
                                File dir = new File((Utils.isExternalStorageWritable()) ? getContext().getExternalFilesDir(null) : getContext().getFilesDir(),
                                        getContext().getString(R.string.backup_dir_name));
                                if(!dir.exists()) {
                                    try {
                                        dir.mkdir();
                                    } catch(SecurityException e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if(dir.exists()) {
                                    data.putExtra("file", dir);
                                    ((SettingsActivity) getContext()).onActivityResult(PICK_DIR_IMPORT_BACKUP_REQUEST_CODE, Activity.RESULT_OK, data);
                                } else {
                                    Toast.makeText(getContext(), getContext().getString(R.string.backup_dir_creating_failure), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            if (getContext() instanceof Activity) {
                                ((Activity) getContext()).startActivityForResult(intent, PICK_DIR_IMPORT_BACKUP_REQUEST_CODE);
                            }
                        }
                    }
                });
            }
        }
    }
}
