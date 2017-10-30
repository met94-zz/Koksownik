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

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by abara on 7/25/2017.
 */

public class DetailsListAdapter extends ArrayAdapter<HashMap<String, String>> implements View.OnFocusChangeListener {
    // declaring our ArrayList of items
    private List<HashMap<String, String>> mObjects;
    private @LayoutRes
    int mResourceId;
    int mSeparatorId;
    DetailsFragment fragment;

    public DetailsListAdapter(@NonNull Context context, @LayoutRes int resource, @LayoutRes int separator, @NonNull List<HashMap<String, String>> objects, DetailsFragment fragment_) {
        super(context, resource, objects);
        mObjects = objects;
        mResourceId = resource;
        mSeparatorId = separator;
        fragment = fragment_;
    }

    @Override
    public int getItemViewType(int position) {
        HashMap<String, String> map = mObjects.get(position);
        boolean isSeparator = map.containsKey("separator");
        if(isSeparator) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent){
        View v = convertView;
        HashMap<String, String> map = mObjects.get(position);
        boolean isSeparator = map.containsKey("separator");

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(isSeparator) {
                v = inflater.inflate(mSeparatorId, null);
                TextView textView = (TextView)v.findViewById(R.id.details_list_TextView);
                String timestamp = map.get("timestamp");
                textView.setTag(timestamp);
                textView.setText(convertTimestampToDate(timestamp));
                return v;
            } else {
                v = inflater.inflate(mResourceId, null);
            }
        }

        if(!isSeparator) {
            TextView textView = (TextView) v.findViewById(R.id.details_list_TextView1);
            if(textView != null) {
                textView.setText(map.get("title").replace('_', ' '));
                textView.setTag(position);
                textView.setOnLongClickListener(fragment);
            }

            EditText editText = (EditText) v.findViewById(R.id.details_list_EditText1);
            if(editText != null) {
                editText.setText(map.get("data"));
                //editText.setTag(position);
                editText.setTag(map);
                editText.setOnFocusChangeListener(this);
            }
        } else {
            TextView textView = (TextView)v.findViewById(R.id.details_list_TextView);
            String timestamp = map.get("timestamp");
            //if(!((String)(textView.getTag())).equals(timestamp)) {
            if(!textView.getTag().equals(timestamp)) {
                textView.setText(convertTimestampToDate(timestamp));
            }
        }

        // the view must be returned to our activity
        return v;

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        switch(id) {
            case R.id.details_list_EditText1:
                if(!hasFocus) {
                    HashMap<String, String> map = (HashMap<String, String>)v.getTag();
                    if (map != null && map.containsKey("path")) {
                        String path = map.get("path");
                        if (!path.isEmpty()) {
                            Tree<String> tree = fragment.tree.getChildTreeByPath(path);
                            String newData = ((EditText) v).getText().toString();
                            if (tree.getData() != null && !tree.getData().equals(newData)) {
                                tree.setData(newData);
                                map.put("data", newData);
                            }
                        }
                    }
                }
            default:
                break;
        }
    }

    private String convertTimestampToDate(String timestamp)
    {
        String date;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", new Locale("pl", "PL"));//new SimpleDateFormat("MM/dd/yyyy HH:mm");
        if(timestamp != null) {
            date = format.format(new Date(Long.valueOf(timestamp)));
        } else {
            date = "";
        }
        return date;
    }

}
