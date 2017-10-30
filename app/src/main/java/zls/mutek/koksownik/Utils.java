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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by abara on 8/4/2017.
 */

public class Utils {
    static final float[] NEGATIVE_COLORFILTER = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };
    public final static int REQUEST_CODE_ASK_PERMISSIONS = 123;


    public static void showAlertDialog(Context context, @StringRes int title, @StringRes int message)
    {
        showAlertDialog(context, context.getString(title), context.getString(message));
    }

    public static void showAlertDialog(Context context, @StringRes int title, @StringRes int message, DialogInterface.OnClickListener listener)
    {
        showAlertDialog(context, context.getString(title), context.getString(message), listener);
    }

    public static void showAlertDialog(Context context, String title, @Nullable String message)
    {
        showAlertDialog(context, title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }

    public static void showAlertDialog(Context context, String title, @Nullable String message, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        if(message != null) {
            builder.setMessage(message);
        }
        builder.setPositiveButton(R.string.ok, listener);
        builder.create().show();
    }

    /**********************************
     *
     * askPermissions
     * asks for missing permissions
     *
     **********************************/
    public static boolean askPermissions(Activity activity, String[] perms)
    {
        int i=0;
        for(int j=0; j<perms.length; j++) {
            if(ContextCompat.checkSelfPermission( activity, perms[j]) == PackageManager.PERMISSION_DENIED) {
                perms[i++] = perms[j];
            }
        }

        String[] permsToAsk = Arrays.copyOf(perms, i);

        if(permsToAsk.length == 0) {
            return true;
        }

        ActivityCompat.requestPermissions(activity, permsToAsk, REQUEST_CODE_ASK_PERMISSIONS);
        return false;
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void restartApplication(Context context)
    {
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage( context.getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }


    public static void saveLogcatToFile(Activity context) {
        File dir = (Utils.isExternalStorageWritable()) ? context.getExternalCacheDir() : context.getCacheDir();
        String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
        File outputFile = new File(dir, fileName);
        try {
            Process process = Runtime.getRuntime().exec("logcat -df " + outputFile.getAbsolutePath());
            Toast.makeText(context, outputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    static boolean checkPreferencesSecurityKey(MainActivity activity)
    {
        String KEY_SECURITYKEY = activity.getString(R.string.generalPreferences_securityKeyKey);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String securityKey = sharedPreferences.getString(KEY_SECURITYKEY, null);
        /*
        if(securityKey == null || securityKey.length() < 12) {
            return false;
        } else {
            String newSecurityKey = getKeyHash(getEncryptedIMEI(activity));
            if(!newSecurityKey.contains(securityKey)) {
                sharedPreferences.edit().putString(KEY_SECURITYKEY, null).apply();
                return false;
            }
        }
        return true;
        */
        return (securityKey != null); //edit to enable Key verification
    }

    static String getKeyHash(String imei)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteHash = digest.digest(imei.getBytes());
            String keyHash = "";
            //for(byte b : byteHash) {
            for(int i=0; i<20; i++) {
                byte b = byteHash[i];
                keyHash += String.format("%1$02X", b);
            }
            return keyHash;
        } catch(NoSuchAlgorithmException e) {
            return "";
        }
    }

    static String getEncryptedIMEI(Context context)
    {
        String result = "";
        final int xorVal = 0x55;
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String key = telephonyManager.getDeviceId();
        if(key == null || key.length() < 5) {
            key = "K0Tl3T";
        }
        for(int i=0; i<key.length() && key.charAt(i) != '\0'; i++)
        {
            result += String.format("%1$02X", key.charAt(i) ^ xorVal);;
        }
        return result;
    }
}
