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
import android.util.Log;
import android.util.Xml;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.extended.parser.XMLChar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by abara on 7/21/2017.
 */

public class XMLUtils {
    private static XMLUtils xmlUtils;
    private MainActivity activity;
    private final String TAG = "XML_TAG";

    public XMLUtils(MainActivity activity_)
    {
        activity = activity_;
    }

    public static XMLUtils getInstance(MainActivity activity)
    {
        if(xmlUtils == null) {
            xmlUtils = new XMLUtils(activity);
        }
        return xmlUtils;
    }

    public Tree<String> parseXmlToTree(Tree<String> tree, InputStream in) throws XmlPullParserException, IOException {
        return parseXmlToTree(tree, in, -1, false);
    }

    public Tree<String> parseXmlToTree(Tree<String> tree, InputStream in, int limit, boolean ignoreExisting) throws XmlPullParserException, IOException {
        if(tree == null) {
            tree = new Tree<String>("root", null);
        }

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        parseXmlToTree_Fill(parser, tree, 0, limit, ignoreExisting);

        return tree;
    }

    public void parseXmlToTree_Fill(XmlPullParser parser, Tree<String> parent) throws XmlPullParserException, IOException
    {
        parseXmlToTree_Fill(parser, parent, 0, -1, false);
    }

    public void parseXmlToTree_Fill(XmlPullParser parser, Tree<String> parent, int depth, int limit, boolean ignoreExisting) throws XmlPullParserException, IOException
    {
        int i=0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                if(parser.getEventType() == XmlPullParser.TEXT) {
                    parent.setData(parser.getText());
                }
                continue;
            }
            if(limit != -1) {
                i++;
                int reqDepth = (limit & 0xFFFF0000) >> 16;
                int elemsLimit = limit & 0xFFFF;
                if(reqDepth == depth) {
                    if(i > elemsLimit) {
                        return;
                    }
                }
            }
            String name = parser.getName();
            String dateAttr = parser.getAttributeValue(null, "date");

            Tree<String> tree = null;
            boolean createTree = (!ignoreExisting || !parent.hasChild(name));
            if(createTree) {
                tree = new Tree<String>(name, parent);
            } else {
                tree = parent.getChild(name);
            }
            if(tree != null && dateAttr != null && !dateAttr.isEmpty()) {
                tree.setData(dateAttr);
            }
            if(createTree) {
                parent.addChild(tree);
            }
            parseXmlToTree_Fill(parser, tree, depth+1, limit, ignoreExisting);
            if(!tree.hasChildren())
                i--;
        }
    }


    public void saveValidatedTreeToXMLRemoveNodes(VTDNav vn, XMLModifier xm, Tree<String> tree) throws Exception
    {
        for(Tree<String> child : tree.getChildren())
        {
            if(child.getDeleteValidXmlEntry())
            {
                vn.toElement(VTDNav.R);
                //int offset = vn.getTokenOffset(vn.getCurrentIndex());
                //String name2 = vn.toRawString(vn.getCurrentIndex());
                //Log.d("OFFSET", name2 + " " + String.valueOf(offset));
                if(saveValidatedTreeToXMLGoToNodeFromRoot(vn, xm, child)) {
                    //offset = vn.getTokenOffset(vn.getCurrentIndex());
                    //name2 = vn.toRawString(vn.getCurrentIndex());
                    //Log.d("OFFSET2", name2 + " " + String.valueOf(offset));
                    long l = vn.getElementFragment();
                    xm.remove(l);
                    //xm.removeToken(vn.getCurrentIndex());
                }
            }
            saveValidatedTreeToXMLRemoveNodes(vn, xm, child);
        }
    }

    public void saveValidatedTreeToXML( Tree<String> tree_, String filename) throws Exception
    {
        VTDGen vg = new VTDGen(); // Instantiate VTDGen
        XMLModifier xm = new XMLModifier(); //Instantiate XMLModifier
        if (vg.parseFile(activity.getFilesDir()+"/"+filename, false)) {
            VTDNav vn = vg.getNav();
            xm.bind(vn);
            try {
                saveValidatedTreeToXMLGoRecursive(vn, xm, tree_);
                if(tree_.shouldDeleteXMLNodes()) {
                    vn = xm.outputAndReparse();
                    xm.bind(vn);
                    saveValidatedTreeToXMLRemoveNodes(vn, xm, tree_);
                }
            } finally {

                xm.output(activity.openFileOutput(filename, Context.MODE_PRIVATE));
            }
        }
    }

    public void saveValidatedTreeToXMLGoRecursive( VTDNav vn, XMLModifier xm, Tree<String> tree) throws Exception
    {
        int i=-1;
        //int offset = vn.getTokenOffset(vn.getCurrentIndex());
        //String name2 = vn.toRawString(vn.getCurrentIndex());
        //Log.d("OFFSET", name2 + " " + String.valueOf(offset));
        String data = tree.getData();
        if (tree.getNeedValidXmlEntry()) {
            if (data != null && !data.isEmpty()) {
                i = vn.getText();
                if (i != -1) {
                    //Log.d("OFFSET_update", name2 + " " + String.valueOf(offset));
                    xm.updateToken(i, data);
                } else {
                    String name = vn.toRawString(vn.getCurrentIndex());
                    Log.w(TAG, "Not text node " + tree.getNodeName() + " " + name);
                    //throw new Exception("Not text node " + tree.getNodeName() + " " + name);
                }
            }
        }

        i = vn.getText();
        if (i != -1 || data != null) {
            if((i != -1 && vn.startsWith(i, "JOP")) || (data != null && data.startsWith("JOP")) ) {
                if(tree.shouldWriteXML()) {
                    //String name = vn.toRawString(i);
                    //name2 = vn.toRawString(vn.getCurrentIndex());
                    if (tree.hasChildren()) {
                        saveValidatedTreeToXML(tree.getChildren().get(0), data + ".xml");
                    }
                }
                vn.toElement(VTDNav.PARENT);
                return;
            }
        }

        for(Tree<String> child : tree.getChildren())
        {
            /*
            if(child.getNodeName().equals("root")) {
                child.getNodeName();
                /*
                if(i != -1) {
                    name2 = vn.toRawString(i);
                    name2.isEmpty();
                }
                */
            //}
            //offset = vn.getTokenOffset(vn.getCurrentIndex());
            //name2 = vn.toRawString(vn.getCurrentIndex());
            //Log.d("OFFSET_B", "\n" + name2 + " " + String.valueOf(offset) + " " + child.getNodeName());

            boolean ret = vn.toElement(VTDNav.FC, child.getNodeName());
            if (!ret) {
                ret = vn.toElement(VTDNav.NS, child.getNodeName());
                if(!ret) {
                    vn = saveValidatedTreeToXMLCreateNode(vn, xm, child);
                    xm.bind(vn);
                    if(!saveValidatedTreeToXMLGoToNodeFromRoot(vn, xm, child)) {
                        throw new Exception("Couldnt create node " + child.getNodeName());
                    }
                }
            }

            //offset = vn.getTokenOffset(vn.getCurrentIndex());
            //name2 = vn.toRawString(vn.getCurrentIndex());
            //Log.d("OFFSET_A", "\n" + name2 + " " + String.valueOf(offset) + " " + child.getNodeName() + " " + String.valueOf(ret));

            saveValidatedTreeToXMLGoRecursive(vn, xm, child);
        }
        vn.toElement(VTDNav.PARENT);
    }

    public VTDNav saveValidatedTreeToXMLCreateNode( VTDNav vn, XMLModifier xm, Tree<String> tree) throws Exception
    {
        String prefix = "";
        String toInsert = "<" + tree.getNodeName() + ">" + prefix + "</" + tree.getNodeName() + ">";
        xm.insertAfterHead(toInsert);
        //xm.insertBeforeTail(toInsert);
        return xm.outputAndReparse();
    }

    public boolean saveValidatedTreeToXMLGoToNodeFromRoot( VTDNav vn, XMLModifier xm, Tree<String> tree) throws Exception
    {
        Tree<String> t = tree.getParent();
        if(t == null) //root
        {
            return vn.toElement(VTDNav.FC, tree.getNodeName());
        } else {
            saveValidatedTreeToXMLGoToNodeFromRoot(vn, xm, tree.getParent());
            return vn.toElement(VTDNav.FC, tree.getNodeName());
        }
    }

    public void validXmlWithTree( Context context, Tree<String> tree_, String filename) throws Exception
    {
        if(tree_ == null) { //probably no need to valid tree that wasnt opened by user
            return;
        }
        Tree<String> tree = tree_;

        try {
            InputStream in = context.openFileInput(filename);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            tree.setAllNeedValidXmlEntry(true);
            tree.setNeedValidXmlEntry(false); //always false for root

            boolean goUp = false;
            while (parser.next() != XmlPullParser.END_DOCUMENT) {

                if (parser.getEventType() == XmlPullParser.END_TAG) {
                    if(goUp) {
                        tree = tree.getParent();
                    }
                    continue;
                }
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    if(parser.getEventType() == XmlPullParser.TEXT) {
                        String text = parser.getText();
                        if(text != null) {
                            tree.setNeedValidXmlEntry( !(text.equals(tree.getData())) );
                            if(text.startsWith("JOP")) {
                                validXmlWithTree(context, tree.getChild("root"), text + ".xml");
                            }
                        }
                    }
                    continue;
                }
                String name = parser.getName();
                if(tree.hasChild(name)) {
                    tree = tree.getChild(name);
                    if(!tree.getDeleteValidXmlEntry()) {
                        tree.setNeedValidXmlEntry(false);
                        goUp = true;
                    } else { //ignore all child nodes because node is getting deleted
                        tree.setAllNeedValidXmlEntry(false);
                        Integer event=0;
                        while(true)
                        {
                            event = parser.next();
                            if(event == XmlPullParser.END_DOCUMENT) {
                                break;
                            }
                            if(event == XmlPullParser.END_TAG) {
                                String curName = parser.getName();
                                if(curName != null) {
                                    if(curName.equals(name)) {
                                        break;
                                    }
                                }
                            }
                        }
                        tree = tree.getParent();
                        goUp = false;
                    }
                } else {
                    goUp = false;
                }
            }

        } catch(FileNotFoundException e) {
            Utils.showAlertDialog(context, R.string.error_occurred, R.string.file_no_exists);
            Log.w(TAG, context.getString(R.string.file_no_exists));
            Log.w(TAG, e.toString());
        } catch (XmlPullParserException e) {
            tree.setAllNeedValidXmlEntry(true);
            tree.setNeedValidXmlEntry(false); //always false for root

            Utils.showAlertDialog(context, R.string.error_occurred, R.string.file_corrupted);
            Log.w(TAG, context.getString(R.string.file_corrupted));
            Log.w(TAG, e.toString());
        } catch (IOException e) {
            Utils.showAlertDialog(context, R.string.error_occurred, R.string.file_unknown_error);
            Log.w(TAG, context.getString(R.string.file_unknown_error));
            Log.w(TAG, e.toString());
        }
    }

    public String createChildFile(Context context) throws IOException
    {
        File childFile = File.createTempFile("JOP", ".xml", context.getFilesDir());
        String childFileName = childFile.getName();
        createEmptyXML(childFileName);
        return childFileName;
    }

    public void createInitialXML(String filename) throws IOException
    {
        FileOutputStream fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, true);
        //serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");
        serializer.startTag(null, "calypso");
        serializer.startTag(null, "bic");
        serializer.startTag(null, "modlitewnik");
        serializer.text("JOP435235");
        serializer.endTag(null, "modlitewnik");
        serializer.startTag(null, "brama");
        serializer.text("JOP8657867");
        serializer.endTag(null, "brama");
        serializer.endTag(null, "bic");
        serializer.endTag(null, "calypso");
        serializer.endTag(null, "root");
        serializer.endDocument();

        serializer.flush();

        fos.close();

        createInitialXML2("JOP435235.xml");
        createInitialXML2("JOP8657867.xml");
    }

    public void createInitialXML2(String filename) throws IOException
    {
        FileOutputStream fos = activity.openFileOutput(filename, Context.MODE_PRIVATE);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, true);
        //serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");
        String name = "T"+String.valueOf(new Date().getTime() - 100*60*60*24*24);
        serializer.startTag(null, name);
        serializer.startTag(null, "seria");
        serializer.text("99kg");
        serializer.endTag(null, "seria");
        serializer.startTag(null, "seria2");
        serializer.text("33kg");
        serializer.endTag(null, "seria2");
        serializer.endTag(null, name);
        name = "T"+String.valueOf(new Date().getTime() - 100*60*60*24*28*2);
        serializer.startTag(null, name);
        serializer.startTag(null, "seria3");
        serializer.text("betonik");
        serializer.endTag(null, "seria3");
        serializer.startTag(null, "seria4");
        serializer.text("na telefonik");
        serializer.endTag(null, "seria4");
        serializer.endTag(null, name);
        name = "T"+String.valueOf(new Date().getTime() - 100*60*60*24*32*3);
        serializer.startTag(null, name);
        serializer.startTag(null, "seria");
        serializer.text("beton");
        serializer.endTag(null, "seria");
        serializer.startTag(null, "seria2");
        serializer.text("na telefon");
        serializer.endTag(null, "seria2");
        serializer.endTag(null, name);
        serializer.endTag(null, "root");
        serializer.endDocument();

        serializer.flush();

        fos.close();
    }

    public void createEmptyXML(String filename) throws IOException
    {
        byte[] emptyDoc = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><root></root>".getBytes("UTF-8");
        try {
            VTDGen vg = new VTDGen();
            vg.setDoc(emptyDoc);
            vg.parse(false);
            VTDNav vn = vg.getNav();
            XMLModifier xm = new XMLModifier(vn);
            xm.output(activity.openFileOutput(filename, Context.MODE_PRIVATE));
        } catch(Exception e) {

        }
/*
        FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, true);
        //serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");
        serializer.startTag(null, "dummy");
        serializer.endTag(null, "dummy");
        serializer.endTag(null, "root");
        serializer.endDocument();

        serializer.flush();

        fos.close();
*/
    }

    /**
     * Check to see if a string is a valid Name according to [5]
     * in the XML 1.0 Recommendation
     *
     * @param name string to check
     * @return true if name is a valid Name
     */
    public static boolean isValidXMLName(String name) {
        if (name.length() == 0)
            return false;
        char ch = name.charAt(0);
        if(!XMLChar.isNameStartChar(ch) || ch == ':' || ch == ';')
            return false;
        for (int i = 1; i < name.length(); i++ ) {
            ch = name.charAt(i);
            if(!XMLChar.isNameChar( ch ) || ch == ':' || ch == ';' || ch == ' '){
                return false;
            }
        }
        return true;
    }

    public static String fixXMLName(String name) {
        StringBuilder sb = new StringBuilder();
        char ch;
        for(int i=0; i<name.length(); i++) {
            ch = name.charAt(i);
            if(!XMLChar.isNameChar(ch) || ch == ':' || ch == ';' || ch == ' ') {
                sb.append("_");
            } else sb.append(ch);
        }
        return sb.toString();
    }

    public static String validXMLName(Tree<String> tree, String name) {
        char ch = name.charAt(0);
        if(Character.getType(ch) != Character.UPPERCASE_LETTER && Character.getType(ch) != Character.LOWERCASE_LETTER) { //first char must be a letter
            name = 'L' + name;
        }
        if(!XMLUtils.isValidXMLName(name)) { //any invalid characters?
            name = XMLUtils.fixXMLName(name); //fix invalid characters
        }
        while(tree.hasChild(name)) { //check for duplicates
            if(name.charAt(name.length()-1) == '9' || !Character.isDigit(name.charAt(name.length()-1))) {
                name += '1';
            } else {
                char c = name.charAt(name.length()-1);
                name = name.substring(0, name.length()-1) + (++c);
            }
        }
        return name;
    }
}
