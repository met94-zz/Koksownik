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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by abara on 7/19/2017.
 */

public class Tree<T> {

    private String nodeName;
    private T data;
    private Tree<T> parent;
    private List<Tree<T>> children;
    private boolean needValidXmlEntry = false;
    private boolean deleteXmlEntry = false;

    public Tree(String name, Tree<T> parent_) {
        nodeName = name;
        children = new ArrayList<Tree<T>>();
        parent = parent_;
    }

    public void addChild(Tree<T> child) {
        children.add(child);
    }

    public void addChild(Tree<T> child, int index) {
        children.add(index, child);
    }

    public boolean hasChildren() {
        return (children != null && !children.isEmpty());
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String name) {
        nodeName = name;
    }

    public void setChildren(List<Tree<T>> children_) {
        children = children_;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public void setData(T data_) {
        data = data_;
    }

    public T getData() {
        return data;
    }

    public Tree<T> getParent() {
        return parent;
    }

    public void setParent(Tree<T> parent_) {
        parent = parent_;
    }

    public void setAllNeedValidXmlEntry(boolean val) {
        needValidXmlEntry = val;
        for (Tree<T> child : children) {
            child.setAllNeedValidXmlEntry(val);
        }
    }

    public void setNeedValidXmlEntry(boolean val) {
        needValidXmlEntry = val;
    }

    public boolean getNeedValidXmlEntry() {
        return needValidXmlEntry;
    }

    public void setDeleteXmlEntry(boolean val) {
        deleteXmlEntry = val;
    }

    public boolean getDeleteValidXmlEntry() {
        return deleteXmlEntry;
    }

    public boolean shouldWriteXML() {
        boolean ret = getNeedValidXmlEntry();
        if (ret) {
            return true;
        }
        for (Tree<T> child : children) {
            ret = child.shouldWriteXML();
            if (ret) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldDeleteXMLNodes()
    {
        boolean ret = getDeleteValidXmlEntry();
        if (ret) {
            return true;
        }
        for (Tree<T> child : children) {
            ret = child.shouldDeleteXMLNodes();
            if (ret) {
                return true;
            }
        }
        return false;
    }

    public boolean getAllChildrenDeleteValidXmlEntry()
    {
        if(children.isEmpty())
            return false;
        for (Tree<T> child : children) {
            if(!child.getDeleteValidXmlEntry())
                return false;
        }
        return true;
    }

    public boolean hasChild(String name)
    {
        if(name.isEmpty()) {
            return false;
        }
        if(hasChildren())
        {
            for(Tree<T> child : children)
            {
                if(child.nodeName.equals(name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Tree<T> getChild(String name)
    {
        if(hasChild(name))
        {
            for(Tree<T> child : children)
            {
                if(child.nodeName.equals(name))
                {
                    return child;
                }
            }
        }
        return null;
    }

    public Tree<T> getChild(int index)
    {
        if(hasChildren())
        {
            return children.get(index);
        }
        return null;
    }

    @Deprecated
    public Tree<T> getChildTreeByNodeName(String node)
    {
        if(nodeName.equals(node))
        {
            return this;
        } else if(hasChildren())
        {
            for(Tree<T> child : children)
            {
                Tree<T> t = child.getChildTreeByNodeName(node);
                if(t != null) {
                    return t;
                }
            }
        }
        return null;
    }

    public Tree<T> getChildTreeByPath(String path)
    {
        List<String> split = new ArrayList<String>(Arrays.asList(path.split("/")));
        if(!split.isEmpty()) {
            if(nodeName.equals(split.get(0))) {
                split.remove(0);
                if(split.isEmpty()) {
                    return this;
                }
            }
        }
        return getChildTreeByPath(split);
    }

    public Tree<T> getChildTreeByPath(List<String> path)
    {
        if(path.isEmpty()) {
            return null;
        }
        if(hasChildren()) {
            for(Tree<T> child : children)
            {
                if(child.nodeName.equals(path.get(0)))
                {
                    path.remove(0);
                    if(path.isEmpty()) {
                        return child;
                    }
                    return child.getChildTreeByPath(path);
                }
            }
        }
        if(!path.isEmpty())
        {
            if(nodeName.equals(path.get(0)))
            {
                return this;
            }
        }
        return null;
    }
}
