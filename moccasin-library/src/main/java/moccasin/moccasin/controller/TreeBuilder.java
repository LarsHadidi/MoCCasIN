package moccasin.moccasin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import moccasin.moccasin.model.TreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.contains;

public class TreeBuilder {
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<TreeNode> icdTree = new ArrayList<>();
    private List<TreeNode> atcTree = new ArrayList<>();
    private IndexedCollection<TreeNode> icdSearchIndex;
    private IndexedCollection<TreeNode> atcSearchIndex;
    
    private final static Logger LOGGER = Logger.getLogger(TreeBuilder.class.getName());


    /**
     * Constructor.
     * Initializes both icdTree and atcTree, then rebuilds an search index of the leaves for both trees separately.
     * @param resourcePath Path to the serialized catalog files.
     */
    public TreeBuilder(File resourcePath) {
       buildTree(resourcePath, "icd");
       buildTree(resourcePath, "atc");
    }

    private void buildTree(File resourcePath, String catalog) {
        File file = new File(resourcePath, catalog);
        try {
            LOGGER.info("Reading " + file);
            switch (catalog) {
                case "icd":
                    icdTree = readTree(file);
                    LOGGER.info("Building index into primary memory");
                    icdSearchIndex = initIndex();
                    buildIndex(icdTree, icdSearchIndex);
                    break;
                case "atc":
                    atcTree = readTree(file);
                    LOGGER.info("Building index into primary memory");
                    atcSearchIndex = initIndex();
                    buildIndex(atcTree, atcSearchIndex);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            LOGGER.severe("TREE COULD NOT BE BUILT: " + e.getMessage());
        }
    }

    /**
     * Method to retrieve a complete tree.
     * @param type The kind of tree, either icd or atc.
     * @return An ArrayNode-Object containing the tree.
     */
    public ArrayNode getTree(String type) {
        switch(type) {
            case "icd":
                if (icdTree == null) throw new NullPointerException("icdTree is null");
                return treesToArrayNode(icdTree, 1, 5);
            case "atc":
                if (atcTree == null) throw new NullPointerException("atcTree is null");
                return treesToArrayNode(atcTree, 1, 5);
            default:
                throw new IllegalArgumentException(type);
        }
    }

    /**
     * Method to retrieve a subtree whose leaves contain the given search string.
     * @param searchString The string to search in the leaves.
     * @param type The kind of tree, either icd or atc.
     * @return An ArrayNode-Object containing the subtree.
     */
    public ArrayNode searchTree(String searchString, String type) {
        switch(type) {
            case "icd":
                if (icdSearchIndex == null) throw new NullPointerException("icdSearchIndex is null");
                return searchTreeLeaves(searchString, icdSearchIndex);
            case "atc":
                if (atcSearchIndex == null) throw new NullPointerException("atcSearchIndex is null");
                return searchTreeLeaves(searchString, atcSearchIndex);
            default:
                throw new IllegalArgumentException(type);
        }
    }

    /**
     * Method to initialize the IndexedCollection object to use as an index.
     * The collection uses a HashIndex for the CODE attrubute and a SuffixTreeIndex for the LOWERCASE_LABEL attribute.
     * @return The allocated collection.
     */
    private IndexedCollection<TreeNode> initIndex() {
        IndexedCollection<TreeNode> searchIndex = new ConcurrentIndexedCollection<>();
        searchIndex.addIndex(HashIndex.onAttribute(TreeNode.CODE_ATTR));
        searchIndex.addIndex(SuffixTreeIndex.onAttribute(TreeNode.LOWERCASE_LABEL_ATTR));
        return searchIndex;
    }

    /**
     * Method to rebuild a search index collection from a list of trees.
     * @param treeNodes A list of trees representing a catalog.
     * @param index A reference to the index collection to be used.
     */
    private void buildIndex(List<TreeNode> treeNodes, IndexedCollection<TreeNode> index) {
           treeNodes.forEach(treeNode -> {
               if(treeNode.hasChildren()) {
                   buildIndex(treeNode.getChildren(), index);
               }
               if(treeNode.isLeaf()) index.add(treeNode);
           });
    }

    /**
     * Method to read a catalog from a file containing a forest.
     * @param catalogFile The file to load from.
     * @return A list of trees representing a catalog.
     */
    private List<TreeNode> readTree(File catalogFile) {
        try (FileInputStream fileInputStream = new FileInputStream(catalogFile)) {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return  (List<TreeNode>)objectInputStream.readObject();
        } catch (IOException|ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Method to convert recursively a forest of trees of TreeNode object to an ArrayNode object representing the same forest
     * in a datastructure suitable for the frontend.
     * @param treeNodes A list of trees representing a catalog.
     * @param CURRENT_LEVEL The current level of recursion depth. Should start with one.
     * @param MAX_LEVEL The maximum depth of the trees in the list.
     * @return An ArrayNode object representing the catalog.
     */
    private ArrayNode treesToArrayNode(List<TreeNode> treeNodes, final int CURRENT_LEVEL, final int MAX_LEVEL) {
        if(treeNodes.isEmpty()) {
            return objectMapper.createArrayNode();
        }
        else {
            if (CURRENT_LEVEL == MAX_LEVEL) {
                return listToArrayNode(treeNodes);
            } else {
                ArrayNode arrayNode = objectMapper.createArrayNode();
                treeNodes.forEach(treeNode -> {
                    ObjectNode parent = objectMapper.createObjectNode();
                    parent.put("text", treeNode.getCode() + ": " + treeNode.getLabel());
                    parent.set("children", treesToArrayNode(treeNode.getChildren(), CURRENT_LEVEL + 1, MAX_LEVEL));
                    arrayNode.add(parent);
                });
                return arrayNode;
            }
        }
    }

    /**
     * Method to convert a flat list of TreeNode objects at leaf level to an ArrayNode object.
     * @param list A list of leaf nodes.
     * @return An ArrayNode object containing the leaves.
     */
    private ArrayNode listToArrayNode(List<TreeNode> list) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        list.forEach(treeNode -> {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("text", treeNode.getCode() + ": " + treeNode.getLabel());

            ObjectNode metadataNode = objectMapper.createObjectNode();
            treeNode.getMetadataStream().forEach(entry -> metadataNode.put(entry.getKey(), entry.getValue().toString()));
            objectNode.put("data", metadataNode.toString());

            arrayNode.add(objectNode);
        });
        return arrayNode;
    }

    /**
     * Method to recursively build the top-down representation of trees in a forest from a list of references to leaves of a forest.
     * The leaves in the source datastructure are supposed to reference their parents. The target datastructure represents nodes in a form
     * suitable for the frontend.
     * @param currentTreeNode The current TreeNode object to ascend from.
     * @param currentObjectNode The current ObjectNode object to transfer data to.
     * @param parentIndexMap An index containing visited parent TreeNode objects and their corresponding ObjectNode objects.
     * @param target Reference to the target datastructure.
     * @param roots A list to keep track of the transformed trees in the forest.
     */
    private void createBottomUpPath(TreeNode currentTreeNode, ObjectNode currentObjectNode, HashMap<TreeNode, ObjectNode> parentIndexMap, ArrayNode target, List<TreeNode> roots) {
        if(currentTreeNode.hasParent()) {
            TreeNode parentTreeNode = currentTreeNode.getParent();
            currentObjectNode.put("text", currentTreeNode.getCode() + ": " + currentTreeNode.getLabel());

            ObjectNode metadataNode = objectMapper.createObjectNode();
            currentTreeNode.getMetadataStream().forEach(entry -> metadataNode.put(entry.getKey(), entry.getValue().toString()));
            currentObjectNode.put("data", metadataNode.toString());

            ObjectNode parent;
            ArrayNode children;

            if(parentIndexMap.containsKey(parentTreeNode)) {
                parent = parentIndexMap.get(parentTreeNode);

                children = (ArrayNode) parent.get("children");
                boolean existing = false;
                Iterator<JsonNode> childrenIterator = children.iterator();
                while(childrenIterator.hasNext() && !existing) {
                    existing = childrenIterator.next().get("text").textValue().equals(currentObjectNode.get("text").textValue());
                }
                if(!existing) children.add(currentObjectNode);
                parent.set("children", children);
            }
            else {
                parent = objectMapper.createObjectNode();
                children = objectMapper.createArrayNode();
                children.add(currentObjectNode);
                parent.set("children", children);
                parentIndexMap.put(parentTreeNode, parent);
            }
            createBottomUpPath(parentTreeNode, parent, parentIndexMap, target, roots);
        }
        else {
            if(roots.contains(currentTreeNode) == false) {
                roots.add(currentTreeNode);
                parentIndexMap.put(currentTreeNode, currentObjectNode);
                currentObjectNode.put("text", currentTreeNode.getCode() + ": " + currentTreeNode.getLabel());
                target.add(currentObjectNode);
            }
        }
    }


    /**
     * Method to build a subtree filtered by leaves matching the search words.
     * @param searchString String containing the words to search for.
     * @param searchIndex Search index to perform the search on.
     * @return An ArrayNode object representing the subtree.
     */
    private ArrayNode searchTreeLeaves(String searchString, IndexedCollection<TreeNode> searchIndex) {
        String[] searchWords = searchString.split(" ");

        Query<TreeNode> searchQuery;
        searchQuery = contains(TreeNode.LOWERCASE_LABEL_ATTR, searchWords[0].toLowerCase());
        if (searchWords.length > 1) {
            for (int i = 1; i < searchWords.length; ++i) {
                searchQuery = and(searchQuery, contains(TreeNode.LOWERCASE_LABEL_ATTR, searchWords[i].toLowerCase()));
            }
        }

        ResultSet<TreeNode> searchResult = searchIndex.retrieve(searchQuery);

        ArrayNode resultArrayNode = objectMapper.createArrayNode();
        HashMap<TreeNode, ObjectNode> parentIndexMap = new HashMap<>();
        ArrayList<TreeNode> forest = new ArrayList<>();
        searchResult.forEach(treeNode -> createBottomUpPath(treeNode, objectMapper.createObjectNode(), parentIndexMap, resultArrayNode, forest));

        return resultArrayNode;
    }
}
