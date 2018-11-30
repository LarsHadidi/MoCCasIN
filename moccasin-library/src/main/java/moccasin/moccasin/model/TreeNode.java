package moccasin.moccasin.model;

import com.googlecode.cqengine.attribute.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

public class TreeNode implements Serializable {
    private final String CODE;
    private final String LABEL;
    private final String LOWERCASE_LABEL;
    private final Integer LEVEL;
    private final TreeNode PARENT;
    private final boolean IS_LEAF;
    private Map<String, Object> metadata;
    private List<TreeNode> children;

    public TreeNode(TreeNode parent, String code, String label, Integer level, boolean isLeaf) {
        this.CODE = code;
        this.LABEL = label;
        this.LOWERCASE_LABEL = label.toLowerCase();
        this.LEVEL = level;
        this.PARENT = parent;
        this.IS_LEAF = isLeaf;

        this.metadata = new HashMap<>();
        this.children = new ArrayList<>();
    }

    public String getCode() {
        return CODE;
    }

    public String getLabel() {
        return LABEL;
    }

    public String getLowercaseLabel() { return LOWERCASE_LABEL; }

    public Integer getLevel() {
        return LEVEL;
    }

    public TreeNode getParent() { return PARENT; }

    public boolean isLeaf() { return IS_LEAF; }

    public void addMetadataEntry(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadataEntry(String key) {
        return metadata.get(key);
    }

    public Stream<Map.Entry<String, Object>> getMetadataStream() {
        return metadata.entrySet().stream();
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public boolean hasParent() {
        return PARENT != null;
    }

    public boolean hasChildren() {
        return children.size() != 0;
    }

    public List<TreeNode> getChildren(){
        return children;
    }

    public static final Attribute<TreeNode, String> CODE_ATTR = attribute(TreeNode::getCode);
    public static final Attribute<TreeNode, String> LABEL_ATTR = attribute(TreeNode::getLabel);
    public static final Attribute<TreeNode, String> LOWERCASE_LABEL_ATTR = attribute(TreeNode::getLowercaseLabel);
    public static final Attribute<TreeNode, Integer> LEVEL_ATTR = attribute(TreeNode::getLevel);
}
