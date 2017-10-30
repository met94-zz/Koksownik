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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.ximpleware.extended.parser.XMLChar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abara on 7/19/2017.
 */

public class MainListFragment extends ListFragment {

    String path;
    Tree<String> tree;
    ArrayList<HashMap<String, String>> adapterItems = new ArrayList<HashMap<String, String>>();

    private final String TAG = "MF_TAG";

    public MainListFragment()
    {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            @SuppressWarnings("ResourceType") View listContainer = v.findViewById(0x00ff0003);//ListFragment.INTERNAL_LIST_CONTAINER_ID);
            if(listContainer != null) {
                float scale = getResources().getDisplayMetrics().density;
                float sizeInDp = 10.0f;
                int dpAsPixels = (int) (sizeInDp*scale + 0.5f);
                listContainer.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, 0);

                ListView listView = (ListView)listContainer.findViewById(android.R.id.list);
                if(listView != null) {
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.remove).setMessage(R.string.remove_node).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //tree.getChild(position).setDeleteXmlEntry(true);
                                    HashMap<String, String> map = (HashMap<String, String>)adapterItems.get(position);
                                    String childName = map.get("title");
                                    if(childName != null && !childName.isEmpty()) {
                                        Tree<String> child = tree.getChild(childName);
                                        child.setDeleteXmlEntry(true);
                                        child.getChildren().clear();
                                        adapterItems.remove(position);
                                        ((MainListAdapter) getListAdapter()).notifyDataSetChanged();
                                    }
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.create().show();
                            return true;
                        }
                    });
                }
            }
        }
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(path != null) {
            ((MainActivity)getActivity()).pathTextView.setText(path.substring(path.indexOf("/")+1));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.actionbar_add:

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_add);
                dialog.setTitle(R.string.add);
                final EditText et = (EditText)dialog.findViewById(R.id.dialog_add_editText);
                et.setFilters(new InputFilter[] {
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end,
                                                       Spanned dest, int dstart, int dend) {
                                if(source.length() > 0) {
                                    char ch = source.charAt(0);
                                    if(ch == ':') {// || ch == ' ') {
                                        return "_";
                                    }
                                    if(dstart > 0 && !XMLChar.isNameChar( ch ) && ch != ' ') {
                                        return "";
                                    }
                                    if (dstart == 0 && !XMLChar.isNameStartChar(ch)) {
                                        return "";
                                    }
                                }
                                return null;
                            }
                        }
                });
                final CheckBox checkBox = ((CheckBox)dialog.findViewById(R.id.dialog_add_CheckBox));
                Button bt = (Button)dialog.findViewById(R.id.dialog_add_Button);
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newNode = et.getText().toString();
                        if(newNode != null && !newNode.isEmpty() && validTree()) {

                            Tree<String> child = new Tree<String>(null, tree); //set node name later
                            if(!checkBox.isChecked()) {
                                try {
                                    String childFileName = XMLUtils.getInstance((MainActivity)getActivity()).createChildFile(getActivity());
                                    child.setData(childFileName.substring(0, childFileName.lastIndexOf(".")));
                                } catch( java.io.IOException e) {
                                    child = null;
                                    Utils.showAlertDialog(MainListFragment.this.getActivity(), R.string.error_occurred, R.string.file_unknown_error);
                                    Log.w(TAG, getString(R.string.file_unknown_error));
                                    Log.w(TAG, e.toString());
                                }
                            }
                            if(child != null) { //do not add if exception occurred
                                newNode = XMLUtils.validXMLName(tree, newNode);
                                child.setNodeName(newNode);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("title", newNode);
                                adapterItems.add(map);

                                tree.addChild(child);
                                if (getListAdapter() == null) {
                                    setListAdapter(new MainListAdapter(getActivity(), R.layout.main_list_layout, adapterItems, MainListFragment.this));
                                }

                                ((MainListAdapter) getListAdapter()).notifyDataSetChanged();
                            }
                        }/* else {
                            Utils.showAlertDialog(MainListFragment.this.getActivity(), R.string.error_occurred, R.string.internal_error);
                        }
                        */
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validTree()
    {
        if(path == null)
        {
            path = (getArguments() != null) ? getArguments().getString("path") : "";
            ((MainActivity)getActivity()).pathTextView.setText(path.substring(path.indexOf("/")+1));
        }

        if(tree == null && getActivity() != null && path != null && !path.isEmpty()) {
            tree = ((MainActivity) getActivity()).getTree().getChildTreeByPath(path);
        }
        return (tree != null);
    }

    @Override
    public void onAttach (Context context)
    {
        super.onAttach(context);

        if(validTree()) {
            if (tree.hasChildren()) {
                for (Tree<String> child : tree.getChildren()) {
                    if(!child.getDeleteValidXmlEntry()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("title", child.getNodeName());
                        adapterItems.add(map);
                    }
                }
                setListAdapter(new MainListAdapter(context, R.layout.main_list_layout, adapterItems, this));
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HashMap<String, String> map = (HashMap<String, String>)adapterItems.get(position);
        Fragment fr=null;

        String childName = map.get("title");
        Bundle args = new Bundle(1);
        args.putString("path", path + "/" + childName);
        Tree<String> child = tree.getChild(childName);
        if(child != null)
        {
            String data = child.getData();
            if(data != null && data.startsWith("JOP") && new File(getActivity().getFilesDir()+"/"+data+".xml").exists()) {
                fr = new DetailsFragment();
                args.putString("id", child.getData());
            }
        }
        if(fr == null) {
            fr = new MainListFragment();

        }
        fr.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fr).addToBackStack(null).commit();
    }
}
