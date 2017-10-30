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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ximpleware.extended.parser.XMLChar;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by abara on 7/20/2017.
 */

public class DetailsFragment extends ListFragment implements View.OnLongClickListener {//AdapterView.OnItemLongClickListener {
    String path;
    String id;
    Tree<String> tree;
    Tree<String> parentTree;
    ArrayList<HashMap<String, String>> adapterItems = new ArrayList<HashMap<String, String>>();
    String newFileTimestamp;
    int limitLoad=0x00000002;

    private final String TAG = "DF_TAG";

    public DetailsFragment()
    {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public boolean onDeleteItem(final int position)
    {
        final HashMap<String, String> map = (HashMap<String, String>)adapterItems.get(position);
        if(map.containsKey("separator")) {
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.remove).setMessage(R.string.remove_node).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String childName = map.get("title");
                String path = map.get("path");
                if(childName != null && !childName.isEmpty()) {
                    Tree<String> child = tree.getChildTreeByPath(path);
                    child.setDeleteXmlEntry(true);
                    child.getChildren().clear();
                    adapterItems.remove(position);
                    ((DetailsListAdapter) getListAdapter()).notifyDataSetChanged();
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

    @Override
    public boolean onLongClick(View view) {
        int position = (int)view.getTag();
        return onDeleteItem(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if(v != null) {
            @SuppressWarnings("ResourceType") FrameLayout listContainer = (FrameLayout)v.findViewById(0x00ff0003);//ListFragment.INTERNAL_LIST_CONTAINER_ID);
            if(listContainer != null) {
                float scale = getResources().getDisplayMetrics().density;
                //int dpAsPixels = (int) (sizeInDp*scale + 0.5f);
                listContainer.setPadding((int) (10*scale + 0.5f), (int) (5*scale + 0.5f), (int) (10*scale + 0.5f), 0);


                ListView listView = (ListView)listContainer.findViewById(android.R.id.list);
                if(listView != null) {
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            return onDeleteItem(position);
                        }
                    });
                }

                Button moreButton = new Button(getContext());
                int _width, _height;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Build.VERSION_CODES.LOLLIPOP=21
                    moreButton.setText(null);
                    _width= ContextCompat.getDrawable(getContext(), R.drawable.btn_rounded_material).getIntrinsicWidth();
                    _height=ContextCompat.getDrawable(getContext(), R.drawable.btn_rounded_material).getIntrinsicHeight();
                } else {
                    moreButton.setText(R.string.plus);
                    _width=ContextCompat.getDrawable(getContext(), R.drawable.ic_history_black_48dp).getIntrinsicWidth();
                    _height=ContextCompat.getDrawable(getContext(), R.drawable.ic_history_black_48dp).getIntrinsicHeight();
                }
                moreButton.setBackgroundResource(R.drawable.btn_rounded_material);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.width = _width;
                lp.height = _height;
                lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                moreButton.setLayoutParams(lp);
                moreButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        limitLoad += 2&0xFFFF;
                        String filename = id + ".xml";
                        File file = getContext().getFileStreamPath(filename);
                        if(file.exists()) {
                            try {
                                InputStream in = getContext().openFileInput(filename);
                                int oldSize = tree.getChildren().size();
                                tree = XMLUtils.getInstance((MainActivity) getActivity()).parseXmlToTree(tree, in, limitLoad, true);
                                if (tree.hasChildren()) {
                                    List<Tree<String>> children = tree.getChildren();
                                    //sort by time
                                    Collections.sort(children, new Comparator<Tree<String>>() {
                                        @Override
                                        public int compare(Tree<String> o1, Tree<String> o2) {
                                            try {
                                                return (int) (Long.parseLong(o2.getNodeName().substring(1)) - Long.parseLong(o1.getNodeName().substring(1)));
                                            } catch(Exception e) {
                                                return 1;
                                            }
                                        }
                                    });
                                    int toLoad = children.size() - oldSize;
                                    HashMap<String, String> map;
                                    //for (int i=children.size()-1; i>(children.size()-1-toLoad); i--) {
                                    for (int i=0; i<toLoad; i++) {
                                        Tree<String> child = children.get(oldSize+i);
                                        if(child.hasChildren() && !child.getAllChildrenDeleteValidXmlEntry()) {
                                            String timestamp = child.getNodeName().substring(1); //Skip first letter 'T'
                                            map = new HashMap<String, String>();
                                            map.put("separator", "true");
                                            map.put("timestamp", timestamp);
                                            adapterItems.add(map);
                                            for (Tree<String> childChild : child.getChildren()) {
                                                if(childChild.getDeleteValidXmlEntry())
                                                    continue;
                                                map = new HashMap<String, String>();
                                                map.put("title", childChild.getNodeName());
                                                map.put("data", childChild.getData());
                                                map.put("path", "root/"+child.getNodeName()+"/"+childChild.getNodeName());
                                                adapterItems.add(map);
                                            }
                                        }
                                        ((DetailsListAdapter) getListAdapter()).notifyDataSetChanged();
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_no_exists);
                                Log.w(TAG, getString(R.string.file_no_exists));
                                Log.w(TAG, e.toString());
                            } catch (XmlPullParserException e) {
                                Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_corrupted);
                                Log.w(TAG, getString(R.string.file_corrupted));
                                Log.w(TAG, e.toString());
                            } catch (IOException e) {
                                Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_unknown_error);
                                Log.w(TAG, getString(R.string.file_unknown_error));
                                Log.w(TAG, e.toString());
                            }
                        }
                    }
                });
                listContainer.addView(moreButton);
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
                dialog.setContentView(R.layout.dialog_add_details);
                dialog.setTitle(R.string.add);
                Button bt = (Button)dialog.findViewById(R.id.dialog_add_Button);
                final EditText et = ((EditText)dialog.findViewById(R.id.dialog_add_editText2));
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
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newNode = et.getText().toString();
                        if(newNode.isEmpty()) {
                            newNode = et.getHint().toString();
                        }
                        if(!newNode.isEmpty()) {
                            String data = ((EditText)dialog.findViewById(R.id.dialog_add_editText3)).getText().toString();


                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            newFileTimestamp = String.valueOf(calendar.getTimeInMillis());

                            String childName = "T" + newFileTimestamp;
                            Tree<String> child = tree.getChild(childName);
                            if (child == null) {
                                child = new Tree<String>(childName, tree);
                                tree.addChild(child);
                            }

                            newNode = XMLUtils.validXMLName(child, newNode);

                            Tree<String> childChild = new Tree<String>(newNode, child);
                            child.addChild(childChild);
                            childChild.setData(data);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("title", newNode);
                            map.put("data", data);
                            map.put("path", "root/" + child.getNodeName() + "/" + childChild.getNodeName());
                            adapterItems.add(0, map);
                            if (getListAdapter() == null) {
                                setListAdapter(new DetailsListAdapter(getActivity(), R.layout.details_list_layout, R.layout.details_list_separator, adapterItems, DetailsFragment.this));
                            }
                            ((DetailsListAdapter) getListAdapter()).notifyDataSetChanged();
                        }
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
        if(id == null)
        {
            id = (getArguments() != null) ? getArguments().getString("id") : "";
        }

        if(path == null)
        {
            path = (getArguments() != null) ? getArguments().getString("path") : "";
            ((MainActivity)getActivity()).pathTextView.setText(path.substring(path.indexOf("/")+1));
        }

        if(getActivity() == null) {
            return false;
        }

        if(parentTree == null && path != null && !path.isEmpty()) {
            parentTree = ((MainActivity) getActivity()).getTree().getChildTreeByPath(path);
        } else return false;

        if(tree == null && id != null && !id.isEmpty()) {
            //tree = ((MainActivity) getActivity()).getTree().getChildTreeByPath(path + "/root"); //in case we already parsed this xml
            tree = parentTree.getChild("root"); //in case we already parsed this xml
            if(tree == null) {
                String filename = id + ".xml";
                File file = getContext().getFileStreamPath(filename);
                if (!file.exists()) {
                    Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_no_exists);
                    Log.w(TAG, getString(R.string.file_no_exists));
                } else {
                    try {
                        InputStream in = getContext().openFileInput(filename);
                        tree = XMLUtils.getInstance((MainActivity) getActivity()).parseXmlToTree(/*tree*/null, in, limitLoad, false);//, 2);
                        //tree.hasChildren();
                    } catch (FileNotFoundException e) {
                        Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_no_exists);
                        Log.w(TAG, getString(R.string.file_no_exists));
                        Log.w(TAG, e.toString());
                    } catch (XmlPullParserException e) {
                        Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_corrupted);
                        Log.w(TAG, getString(R.string.file_corrupted));
                        Log.w(TAG, e.toString());
                    } catch (IOException e) {
                        Utils.showAlertDialog(getActivity(), R.string.error_occurred, R.string.file_unknown_error);
                        Log.w(TAG, getString(R.string.file_unknown_error));
                        Log.w(TAG, e.toString());
                    }
                    if (tree != null) {
                        parentTree.addChild(tree);
                        if (tree.getParent() != null) {
                            Log.w(TAG, getString(R.string.no_parent));
                        }
                        tree.setParent(parentTree);
                    }
                }
            }
        }
        return (tree != null);
    }

    @Override
    public void onAttach (Context context)
    {
        super.onAttach(context);

        if(validTree()) {
            if (tree.hasChildren()) {
                List<Tree<String>> children = tree.getChildren();
                Collections.sort(children, new Comparator<Tree<String>>() {
                    @Override
                    public int compare(Tree<String> o1, Tree<String> o2) {
                        try {
                            return (int) (Long.parseLong(o2.getNodeName().substring(1)) - Long.parseLong(o1.getNodeName().substring(1)));
                        } catch(Exception e) {
                            return 1;
                        }
                    }
                });
                HashMap<String, String> map;
                for (Tree<String> child : children) {
                    if(child.hasChildren() && !child.getAllChildrenDeleteValidXmlEntry()) {
                        String timestamp = child.getNodeName().substring(1); //Skip first letter 'T'
                        map = new HashMap<String, String>();
                        map.put("separator", "true");
                        map.put("timestamp", timestamp);
                        adapterItems.add(map);
                        for (Tree<String> childChild : child.getChildren()) {
                            if(childChild.getDeleteValidXmlEntry())
                                continue;
                            map = new HashMap<String, String>();
                            map.put("title", childChild.getNodeName());
                            map.put("data", childChild.getData());
                            map.put("path", "root/"+child.getNodeName()+"/"+childChild.getNodeName());
                            adapterItems.add(map);
                        }
                    }
                }
                setListAdapter(new DetailsListAdapter(context, R.layout.details_list_layout, R.layout.details_list_separator, adapterItems, this));
            }
        }
    }
}
