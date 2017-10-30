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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by abara on 5/9/2017.
 * settings fragment class
 */

public class SettingsFragment extends PreferenceFragment {

    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mSettingsListener = getOnSharedPreferenceChange();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(mSettingsListener);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mSettingsListener.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(getActivity()), SettingsActivity.KEY_THEME); //set switch text
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
                if(key.equals((SettingsActivity.KEY_THEME))) {
                    if(isAdded()) {
                        SwitchPreference switchPreference = (SwitchPreference) findPreference(getString(R.string.generalPreferences_themeKey));
                        if (switchPreference != null) {
                            if (sharedPreferences.getBoolean(key, true)) {
                                switchPreference.setTitle(R.string.generalPreferences_themeDark);
                            } else {
                                switchPreference.setTitle(R.string.generalPreferences_themeLight);
                            }
                        }
                    }
                }
            }
        };
    }
}