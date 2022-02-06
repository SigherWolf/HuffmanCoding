package huffman;

import huffman.tree.Branch;
import huffman.tree.Leaf;
import huffman.tree.Node;

import java.util.*;
/**
 * The class implementing the Huffman coding algorithm.
 */
public class Huffman {

    /**
     * Build the frequency table containing the unique characters from the String `input' and the number of times
     * that character occurs.
     *
     * @param   input   The string.
     * @return          The frequency table.
     */
    public static Map<Character, Integer> freqTable (String input) {
        if (input == null || input.isEmpty()) {
            return null;
        } else {
            Map<Character, Integer> ft = new HashMap<>();
            for (char c : input.toCharArray()) {
                if (ft.containsKey(c)) {
                    ft.put(c, ft.get(c) + 1);
                } else {
                    ft.put(c, 1);
                }
            }
            return ft;
        }
    }

    /**
     * Given a frequency table, construct a Huffman tree.
     *
     * First, create an empty priority queue.
     *
     * Then make every entry in the frequency table into a leaf node and add it to the queue.
     *
     * Then, take the first two nodes from the queue and combine them in a branch node that is
     * labelled by the combined frequency of the nodes and put it back in the queue. The right-hand
     * child of the new branch node should be the node with the larger frequency of the two.
     *
     * Do this repeatedly until there is a single node in the queue, which is the Huffman tree.
     *
     * @param freqTable The frequency table.
     * @return          A Huffman tree.
     */
    public static Node treeFromFreqTable(Map<Character, Integer> freqTable) {
        if (freqTable == null || freqTable.isEmpty()) {
            return null;
        }

        PQueue pq = new PQueue();
        freqTable.forEach(((character, integer) -> {
            pq.enqueue(new Leaf(character, integer));
        }));

        for (int i = 0; i < freqTable.size(); i++) {
            Node leftChild = pq.dequeue();
            Node rightChild = pq.dequeue();
            Node node;
            if (rightChild != null) {
                node = new Branch(leftChild.getFreq() + rightChild.getFreq(), leftChild, rightChild);
            } else {
                node = leftChild;
            }
            pq.enqueue(node);
        }
        return pq.dequeue();
    }

    /**
     * Construct the map of characters and codes from a tree. Just call the traverse
     * method of the tree passing in an empty list, then return the populated code map.
     *
     * @param tree  The Huffman tree.
     * @return      The populated map, where each key is a character, c, that maps to a list of booleans
     *              representing the path through the tree from the root to the leaf node labelled c.
     */
    public static Map<Character, List<Boolean>> buildCode(Node tree) {
        ArrayList<Boolean> codeMap = new ArrayList<>();
        return tree.traverse(codeMap);
    }

    /**
     * Create the huffman coding for an input string by calling the various methods written above. I.e.
     *
     * + create the frequency table,
     * + use that to create the Huffman tree,
     * + extract the code map of characters and their codes from the tree.
     *
     * Then to encode the input data, loop through the input looking each character in the map and add
     * the code for that character to a list representing the data.
     *
     * @param input The data to encode.
     * @return      The Huffman coding.
     */
    public static HuffmanCoding encode(String input) {
        Map<Character, List<Boolean>> tree = buildCode(treeFromFreqTable(freqTable(input)));
        ArrayList<Boolean> dataList = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            dataList.addAll(tree.get(c));
        }
        return new HuffmanCoding(tree, dataList);
    }

    /**
     * Reconstruct a Huffman tree from the map of characters and their codes. Only the structure of this tree
     * is required and frequency labels of all nodes can be set to zero.
     *
     * Your tree will start as a single Branch node with null children.
     *
     * Then for each character key in the code, c, take the list of booleans, bs, corresponding to c. Make
     * a local variable referring to the root of the tree. For every boolean, b, in bs, if b is false you want to "go
     * left" in the tree, otherwise "go right".
     *
     * Presume b is false, so you want to go left. So long as you are not at the end of the code, so you should set the
     * current node to be the left-hand child of the node you are currently on. If that child does not
     * yet exist (i.e. it is null) you need to add a new branch node there first. Then carry on with the next entry in
     * bs. Reverse the logic of this if b is true.
     *
     * When you have reached the end of this code (i.e. b is the final element in bs), add a leaf node
     * labelled by c as the left-hand child of the current node (right-hand if b is true). Then take the next char from
     * the code and repeat the process, starting again at the root of the tree.
     *
     * @param code  The code.
     * @return      The reconstructed tree.
     */
    public static Node treeFromCode(Map<Character, List<Boolean>> code) {
        Branch root = new Branch(0, null, null);

        for (char c : code.keySet()) {
            Node current = root;
            Iterator<Boolean> boolIterator = code.get(c).iterator();

            // Used Iterator.hasNext() for readability
            while (boolIterator.hasNext()) {
                Boolean currentBool = boolIterator.next();
                if (!currentBool) {
                    if (!boolIterator.hasNext()) {                  // If current boolean is false and the last
                        ((Branch) current).setLeft(new Leaf(c, 0));
                    } else if (((Branch) current).getLeft() == null) {
                        ((Branch) current).setLeft(new Branch(0, null, null));
                    }
                    current = ((Branch) current).getLeft();
                } else {
                    if (!boolIterator.hasNext()) {                  // If current boolean is true and the last
                        ((Branch) current).setRight(new Leaf(c, 0));
                    } else if (((Branch) current).getRight() == null) {
                        ((Branch) current).setRight(new Branch(0, null, null));
                    }
                    current = ((Branch) current).getRight();
                }
            }
        }
        return root;
    }


    /**
     * Decode some data using a map of characters and their codes. To do this you need to reconstruct the tree from the
     * code using the method you wrote to do this. Then take one boolean at a time from the data and use it to traverse
     * the tree by going left for false, right for true. Every time you reach a leaf you have decoded a single
     * character (the label of the leaf). Add it to the result and return to the root of the tree. Keep going in this
     * way until you reach the end of the data.
     *
     * @param code  The code.
     * @param data  The encoded data.
     * @return      The decoded string.
     */
    public static String decode(Map<Character, List<Boolean>> code, List<Boolean> data) {
        Node root = treeFromCode(code);
        Node currentNode = root;

        // Faster performance for concatenating Strings
        StringBuilder result = new StringBuilder();

        for (Boolean currentBool : data) {
            if (!currentBool) {
                currentNode = ((Branch) currentNode).getLeft();
            } else {
                currentNode = ((Branch) currentNode).getRight();
            }

            if (currentNode instanceof Leaf) {
                result.append(((Leaf) currentNode).getLabel());
                currentNode = root;
            }
        }
        return result.toString();
    }
}
