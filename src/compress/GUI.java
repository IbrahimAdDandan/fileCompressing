package compress;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GUI {

    JFrame f;
    JPanel panel;
    JButton rle;
    JButton decompressRle;
    JButton huffComp;
    JButton huffDecomp;
    long orginalSize = 0;
    String parent;
    RLE rleComp;
    Huffman huffman;

    public GUI() {
        this.f = new JFrame();
        this.f.setBounds(500, 100, 760, 500);
        this.f.setTitle("Compress Homework");
        this.panel = new JPanel();
        this.rle = new JButton("RLE Compress");
        this.rle.addActionListener(this.rleCompressing());
        this.panel.add(this.rle);
        this.decompressRle = new JButton("RLE Decompress");
        this.decompressRle.addActionListener(this.rleDecompressing());
        this.panel.add(this.decompressRle);
        this.huffComp = new JButton("Huffman Compress");
        this.huffComp.addActionListener(this.huffCompressing());
        this.panel.add(this.huffComp);
        this.huffDecomp = new JButton("Huffman Decompress");
        this.huffDecomp.addActionListener(this.huffDecompressing());
        this.panel.add(this.huffDecomp);
        this.f.add(this.panel);
        this.f.setVisible(true);
        this.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.rleComp = new RLE();
        this.huffman = new Huffman();
    }

    private String fileChoosing() {
        this.orginalSize = 0;
        Component frame = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (chooser.showOpenDialog(frame) == JFileChooser.OPEN_DIALOG) {
            File file = chooser.getSelectedFile();
            this.parent = file.getParent();
            return file.getPath();
        }
        return null;
    }

    private LinkedList<String> createDirectoryTree(String path) {
        LinkedList<String> tree = new LinkedList<>();
        File file = new File(path);
        tree.add(file.getPath());
        if (file.isFile()) {
            this.orginalSize += file.length();
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                tree.addAll(createDirectoryTree(f.getPath()));
            }
        }
        return tree;
    }

    private byte[] createBytes(LinkedList<String> tree) throws IOException {
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteOs);
        // write length of name
        // write name with sub directories
        // write type (1 for file and 0 for directory)
        // if file write lenght of file
        //       write file content

        for (String p : tree) {
            File file = new File(p);
            String name = file.getPath().replace(this.parent, "");
            byte[] bname = name.getBytes();
            out.writeInt(bname.length);
//            out.write('/');
            out.write(bname);
            if (file.isDirectory()) {
                out.writeInt(0);
            }
            if (file.isFile()) {
                out.writeInt(1);
                byte[] content = Files.readAllBytes(file.toPath());
                out.writeInt(content.length);
                out.write(content);
            }
        }
        byteOs.close();
        out.close();
        return byteOs.toByteArray();
    }

    private void writeDecompressedBytes(byte[] bytes) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            byte[] name;
            int i = 0;
            int shift = 0;
            int type;
            int contentLength;
            byte[] fileContent;
            String filePath = "";
            // read length of name
            // read name with sub directories
            // read type (1 for file and 0 for directory)
            // if directory create directory to this.parent+name
            // if file read lenght of file
            //       read file content
            //       write file content to this.parent+name
            while (i <= bytes.length) {
                filePath = this.parent;
                shift = in.readInt();
                i += 4;
                name = new byte[shift];
                in.read(name, 0, shift);
                i += shift;
                filePath = filePath.concat(new String(name));
                System.out.println("file path: " + filePath);
                type = in.readInt();
                i += 4;
                if (type == 0) {
                    File directory = new File(filePath);
                    System.out.println("Type is: Directory");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                }
                if (type == 1) {
                    System.out.println("Type is: File");
                    contentLength = in.readInt();
                    i += 4;
                    fileContent = new byte[contentLength];
                    in.read(fileContent, 0, contentLength);
                    i += contentLength;
                    Files.write(Paths.get(filePath), fileContent);
                }
            }
        }
    }

    private void writeBytes(byte[] bytes) throws IOException {
        Files.write(Paths.get(this.parent.concat("/compressed.rle")), bytes);
    }

    private ActionListener rleCompressing() {
        return ((ActionEvent ae) -> {
            try {
                String path = this.fileChoosing();
                System.out.println(this.parent);
                LinkedList<String> tree = this.createDirectoryTree(path);
                System.out.println("Tree created");
                byte[] bytes = this.createBytes(tree);
                System.out.println("bytes created");
                // compress bytes
                byte[] compressed = this.rleComp.compress(bytes);
//                double compRatio = (compressed.length/this.orginalSize)*100;
                double compRatio = (compressed.length / bytes.length) * 100;
                System.out.println("Compress Ratio: " + compRatio + "%");
                this.writeBytes(compressed);
                System.out.println("Bytes Wrote");
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, "Done! \nCompress Ratio: " + compRatio + "%");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }

    private ActionListener rleDecompressing() {
        return ((ActionEvent ae) -> {
            try {
                String path = this.fileChoosing();
                File compressedFile = new File(path);
                byte[] file = Files.readAllBytes(compressedFile.toPath());
//                System.out.println(Arrays.toString(file));
                // decompress the file
                byte[] decompressed = this.rleComp.decompress(file);
                System.out.println(Arrays.toString(decompressed));
                this.writeDecompressedBytes(decompressed);
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, "Done!");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }

    private ActionListener huffCompressing() {
        return ((ActionEvent ae) -> {
            try {
                String path = this.fileChoosing();
                System.out.println(this.parent);
                LinkedList<String> tree = this.createDirectoryTree(path);
                System.out.println("Tree created");
                byte[] bytes = this.createBytes(tree);
                System.out.println("bytes created");
                // compress bytes
                byte[] compressed = this.huffman.encode(bytes);
//                double compRatio = (compressed.length/this.orginalSize)*100;
                double compRatio = (compressed.length / bytes.length) * 100;
                System.out.println("Compress Ratio: " + compRatio + "%");
//                this.writeBytes(compressed);
                Files.write(Paths.get(this.parent.concat("/compressed.huff")), compressed);
                System.out.println("Bytes Wrote");
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, "Done! \nCompress Ratio: " + compRatio + "%");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }
    
    private ActionListener huffDecompressing() {
        return ((ActionEvent ae) -> {
            try {
                String path = this.fileChoosing();
                File compressedFile = new File(path);
                byte[] file = Files.readAllBytes(compressedFile.toPath());
//                System.out.println(Arrays.toString(file));
                // decompress the file
                byte[] decompressed = this.huffman.decode(file);
                System.out.println(Arrays.toString(decompressed));
                this.writeDecompressedBytes(decompressed);
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, "Done!");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
    }
}
