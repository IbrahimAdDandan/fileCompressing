
package compress;

import java.io.IOException;
import java.util.Arrays;

public class Test {
    
    public static void main(String[] args) {
        try {
//            RLE rle = new RLE();
//            byte[] b = new byte[] {0,0,0,11,12,12,12,12,1};
//            byte[] compressed = rle.compress(b);
//            System.out.println("compressed: " + Arrays.toString(compressed));
//            byte[] decompressed = rle.decompress(compressed);
//            System.out.println("decompressed: " + Arrays.toString(decompressed));

            Huffman huffman = new Huffman();
            byte[] b = new byte[] {0,0,0,11,12,12,12,12,1,120,125,0,23,34};
            byte[] compressed = huffman.encode(b);
            huffman.decode(compressed);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
