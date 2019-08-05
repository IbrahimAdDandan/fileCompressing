package compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Huffman {

    Map<Byte, String> charPrefixHashMap = new HashMap<>();
    Tree root;
    int countOfNodes;

    private Tree buildTree(Map<Byte, Integer> freq) {
        PriorityQueue<Tree> priorityQueue = new PriorityQueue<>();
        Set<Byte> keySet = freq.keySet();
        for (Byte c : keySet) {

            Tree huffmanNode = new Tree();
            huffmanNode.data = c;
            huffmanNode.frequency = freq.get(c);
            huffmanNode.left = null;
            huffmanNode.right = null;
            priorityQueue.offer(huffmanNode);
        }
        assert priorityQueue.size() > 0;
        this.countOfNodes = priorityQueue.size();

        while (priorityQueue.size() > 1) {

            Tree x = priorityQueue.peek();
            priorityQueue.poll();

            Tree y = priorityQueue.peek();
            priorityQueue.poll();

            Tree sum = new Tree();
            ++this.countOfNodes;
            sum.frequency = x.frequency + y.frequency;
            sum.data = -1;

            sum.left = x;

            sum.right = y;
            root = sum;

            priorityQueue.offer(sum);
        }
        System.out.println("Number of nodes in the tree is : " + this.countOfNodes);
        return priorityQueue.poll();
    }

    private void setPrefixCodes(Tree node, StringBuilder prefix) {

        if (node != null) {
            if (node.left == null && node.right == null) {
                charPrefixHashMap.put(node.data, prefix.toString());

            } else {
                prefix.append('0');
                setPrefixCodes(node.left, prefix);
                prefix.deleteCharAt(prefix.length() - 1);

                prefix.append('1');
                setPrefixCodes(node.right, prefix);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }

    }

    private void buildMaxHeap(Tree node, int index, byte[] treeAsHeap) {

// if not leaf add it's value to the heap
// if left child not null add it's value to treeAsHeap[2*index]
// if right child not null add it's value to treeAsHeap[2*index+1]
        if (node.left != null || node.right != null) {
            treeAsHeap[index] = node.data;
//            System.out.println("node value is: " + node.data);
            if (node.left != null && index*2 < treeAsHeap.length) {
//                System.out.println("left value is: " + node.left.data);
                treeAsHeap[index * 2] = node.left.data;
                buildMaxHeap(node.left, index * 2, treeAsHeap);
            }
            if (node.right != null && index*2+1 < treeAsHeap.length) {
//                System.out.println("right value is: " + node.left.data);
                treeAsHeap[index * 2 + 1] = node.right.data;
                buildMaxHeap(node.right, index * 2 + 1, treeAsHeap);
            }
        }
    }

    public byte[] encode(byte[] test) throws IOException {
        Map<Byte, Integer> freq = new HashMap<>();
        for (int i = 0; i < test.length; i++) {
            if (!freq.containsKey(test[i])) {
                freq.put(test[i], 0);
            }
            freq.put(test[i], freq.get(test[i]) + 1);
        }

        System.out.println("Character Frequency Map = " + freq.toString());
        root = buildTree(freq);

        setPrefixCodes(root, new StringBuilder());
        System.out.println("Character Prefix Map = " + charPrefixHashMap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < test.length; i++) {
            byte c = test[i];
            bos.write(charPrefixHashMap.get(c).getBytes());
        }
        System.out.println("Encoded Length: " + bos.toByteArray().length);
        
        Collection<String> codes = charPrefixHashMap.values();
        int height = 0;
        for(String code: codes) {
            if(code.length() > height) {
                height = code.length();
            }
        }
        System.out.println("height is: " + height);
//        int height = (int) (Math.ceil(Math.log(this.countOfNodes+1) / Math.log(2))+1);
        int size = (int) Math.pow(2, height+1);
//        int size = (int) Math.pow(this.countOfNodes, 2);
//        int size = this.countOfNodes * 4;
        byte[] treeAsHeap = new byte[size];
        Arrays.fill(treeAsHeap, (byte) -1);
        buildMaxHeap(root, 1, treeAsHeap);
//        System.out.println("the tree as heap: " + Arrays.toString(treeAsHeap));
//        System.out.println("content size: " + bos.size());
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteOs);
        out.writeInt(size);
        out.write(treeAsHeap);
        out.writeInt(bos.toByteArray().length);
        out.write(bos.toByteArray());
//        System.out.println("Encoded tree: " + Arrays.toString(treeAsHeap));
//        System.out.println("Encoded content: " + bos.toString());
        return byteOs.toByteArray();
    }

    public byte[] decode(byte[] s) throws IOException {
        // read int as tree length
        // read tree as byte[]
        // read encoded length as int
        // read encoded as String
        ByteArrayInputStream ins = new ByteArrayInputStream(s);
        DataInputStream in = new DataInputStream(ins);
        int treeLength = in.readInt();
        byte[] tree = new byte[treeLength];
        in.read(tree, 0, treeLength);
        int encodedLength = in.readInt();
        byte[] encoded = new byte[encodedLength];
        in.read(encoded, 0, encodedLength);
//        System.out.println("Encoded tree: " + Arrays.toString(tree));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int temp = 1;
        for (int i = 0; i < encoded.length; i++) {
            char j = (char) encoded[i];
//            System.out.println(j);
            if (j == '0' && temp*2+1 < treeLength) {
                temp = temp*2;
                if(tree[temp] != -1) {
//                if (temp*2+1 < treeLength && tree[temp*2] == -1 && tree[temp*2+1] == -1) {
                bos.write(tree[temp]);
                    temp = 1;
                }
            }
            if (j == '1' && temp*2+1 < treeLength) {
                temp = temp*2+1;
                if(tree[temp] != -1) {
//                if (temp*2+1 < treeLength && tree[temp*2] == -1 && tree[temp*2+1] == -1) {
                    bos.write(tree[temp]);
                    temp = 1;
                }
            }
        }

        System.out.println("Decoded is " + Arrays.toString(bos.toByteArray()));
        return bos.toByteArray();
    }
}
