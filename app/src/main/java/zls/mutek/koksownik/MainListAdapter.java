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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by abara on 7/19/2017.
 */

public class MainListAdapter extends ArrayAdapter<HashMap<String, String>> {
    // declaring our ArrayList of items
    private List<HashMap<String, String>> mObjects;
    private @LayoutRes int mResourceId;
    private Context mContext;
    private MainListFragment mFragment;

    public MainListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<HashMap<String, String>> objects, MainListFragment fragment) {
        super(context, resource, objects);
        mContext = context;
        mObjects = objects;
        mResourceId = resource;
        mFragment = fragment;
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent){
        View v = convertView;
        final HashMap<String, String> map = mObjects.get(position);

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(mResourceId, null);
        }

        TextView textView = (TextView) v.findViewById(R.id.main_list_TextView1);
        if(textView != null) {
            textView.setText(map.get("title").replace('_', ' '));
        }

        // the view must be returned to our activity
        return v;

    }

}
