package compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RLE {

    public byte[] compress(byte[] bytes) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte current = bytes[0];
        short count = 1;
        for (int i = 1; i < bytes.length; i++) {
            if (current == bytes[i] && count < 128) {
                count++;
            } else {
                bos.write((byte) count);
                bos.write((byte) current);
                count = 1;
                current = bytes[i];
            }
        }
        bos.write((byte) count);
        bos.write((byte) current);
        return bos.toByteArray();
    }
 
    public byte[] decompress(byte[] bytes) throws IOException {
        ByteArrayInputStream ins = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(ins);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int count = 0;
        byte b;
        int shift = 0;
        System.out.println("compressed Length is: " + bytes.length);
        while (shift < bytes.length) {
            count = new Integer(in.readByte());
//            System.out.println("count is: " + count);
            ++shift;
            b = in.readByte();
            for (int i = 0; i < count; i++) {
                bos.write(b);
            }
            ++shift;
//            System.out.println("shift is: " + shift);
        }
        return bos.toByteArray();
    }
}
