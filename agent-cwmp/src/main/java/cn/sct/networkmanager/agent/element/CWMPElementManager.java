package cn.sct.networkmanager.agent.element;

import cn.sct.agent.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CWMPElementManager {
    private static final Map<String, ElementTreeNode> ELEMENTS = new HashMap<>();

    private static final List<String> FUNCTION_LIST = new ArrayList<>();

    public static void registerFunction(String name) {
        FUNCTION_LIST.add( name);
    }
    public static List<String> getFunctionList() {
        return FUNCTION_LIST;
    }
    public static void registerItemElement(String path, Item<Object> item) {
        ElementTreeNode root;
        String rootName = path.split("\\.")[0];
        if (!ELEMENTS.containsKey(rootName)){
            root=new ElementTreeNode(rootName);
            ELEMENTS.put(rootName,root);
        }
        root=ELEMENTS.get(rootName);
        root.addNodeByPath(path, item);
    }
    public static Item<Object> getItemElement(String path) {
        return ELEMENTS.get(path.split("\\.")[0]).findNodeByPath( path).getValue();
    }

    public static List<ElementTreeNode> getElementTreeNode(String path,int level) {
        return ELEMENTS.get(path.split("\\.")[0]).getNodesAtLevel(path, level);
    }

    public static List<ElementTreeNode> getAllElementTreeNode() {
        List<ElementTreeNode> list = new ArrayList<>();
        for (String key : ELEMENTS.keySet()) {
            ElementTreeNode elementTreeNode = ELEMENTS.get(key);
            list.addAll(elementTreeNode.getValueNodes());
        }
        return list;
    }


}
