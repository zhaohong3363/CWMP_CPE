package cn.sct.networkmanager.agent.element;

import cn.sct.agent.item.Item;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ElementTreeNode {
    private final String name;
    private String fullName;
    private Item<Object> value;
    private Map<String, ElementTreeNode> children;
    private ElementTreeNode parent;

    public ElementTreeNode(String name) {
        this.name = name;
        this.children = new HashMap<>();
    }
    public ElementTreeNode(String name, String fullName) {
        this(name);
        this.fullName = fullName;
    }

    public void addNodeByPath(String path, Item<Object> value) {
        String[] parts = path.split("\\.");
        ElementTreeNode current = this;
        StringBuilder fullNameBuilder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (fullNameBuilder.length() > 0) {
                fullNameBuilder.append(".");
            }
            fullNameBuilder.append(parts[i]);

            String part = parts[i];
            if (!current.children.containsKey(part)) {
                ElementTreeNode child = new ElementTreeNode(part, fullNameBuilder.toString());
                child.parent = current;
                current.children.put(part, child);
            }
            current = current.children.get(part);
        }

        current.value = value;
    }

    public ElementTreeNode findNodeByPath(String path) {
        String[] parts = path.split("\\.");
        ElementTreeNode current = this;

        for (String part : parts) {
            current = current.children.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public Item<?> getValueByPath(String path) {
        ElementTreeNode node = findNodeByPath(path);
        return node != null ? node.value : null;
    }

    public void setValueByPath(String path, Object value) {
        ElementTreeNode node = findNodeByPath(path);
        node.value.setValue( value);
    }

    public List<ElementTreeNode> getValueNodes() {
        List<ElementTreeNode> valueNodes = new ArrayList<>();
        if (this.value !=null){
            valueNodes.add(this);
        }
        for (ElementTreeNode child : children.values()){
            valueNodes.addAll(child.getValueNodes());
        }
        return valueNodes;
    }


    //获取指定path下指定层级的节点
    public List<ElementTreeNode> getNodesAtLevel(String path,int level) {
        ElementTreeNode nodeByPath = findNodeByPath(path);

        List<ElementTreeNode> result = new ArrayList<>();
        if (level == 0) {
            return nodeByPath.getValueNodes();
        }
        if (level == 1) {
            result.addAll(nodeByPath.children.values());
        } else {
            for (ElementTreeNode child : children.values()) {
                result.addAll(child.getNodesAtLevel(level - 1));
            }
        }

        return result;
    }

    private List<ElementTreeNode> getNodesAtLevel(int level) {
        List<ElementTreeNode> result = new ArrayList<>();
        if (level <= 0) {
            return result;
        }

        if (level == 1) {
            result.addAll(children.values());
        } else {
            for (ElementTreeNode child : children.values()) {
                result.addAll(child.getNodesAtLevel(level - 1));
            }
        }

        return result;
    }


}
