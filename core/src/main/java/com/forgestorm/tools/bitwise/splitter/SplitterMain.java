package com.forgestorm.tools.bitwise.splitter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SplitterMain extends ApplicationAdapter {

    private static final BitWiseSplitType SPLIT_TYPE = BitWiseSplitType.SIXTEEN;

    @Getter
    @AllArgsConstructor
    public enum BitWiseSplitType {
        FOUR("BW4", "input/BW4/", 4, 4, 16, 16),
        FOUR_DOUBLE("BW4", "input/BW4H32/", 4, 4, 16, 32),
        SIXTEEN("BW16", "input/BW16/", 10, 5, 16, 16);

        private final String fileNamePrefix;
        private final String getDirectory;
        private final int tilesWide;
        private final int tilesTall;
        private final int tilePixelWidth;
        private final int tilePixelHeight;
    }

    private static final String IMAGE_OUTPUT_DIR = "output/";
    private static final String ATLAS_OUTPUT_DIR = "atlas/";
    private static final String PNG = ".png";

    private final HashMap<Integer, Integer> hashMapBW4;
    private final HashMap<Integer, Integer> hashMapBW16;

    public SplitterMain() {
        hashMapBW4 = new HashMap<>();
        // Row 1
        hashMapBW4.put(0, 5);
        hashMapBW4.put(1, 7);
        hashMapBW4.put(2, 3);
        hashMapBW4.put(3, 1);
        // Row 2
        hashMapBW4.put(4, 13);
        hashMapBW4.put(5, 15);
        hashMapBW4.put(6, 11);
        hashMapBW4.put(7, 9);
        // Row 3
        hashMapBW4.put(8, 12);
        hashMapBW4.put(9, 14);
        hashMapBW4.put(10, 10);
        hashMapBW4.put(11, 8);
        // Row 4
        hashMapBW4.put(12, 4);
        hashMapBW4.put(13, 6);
        hashMapBW4.put(14, 2);
        hashMapBW4.put(15, 0);

        hashMapBW16 = new HashMap<>();
        hashMapBW16.put(0, 22);
        hashMapBW16.put(1, 31);
        hashMapBW16.put(2, 11);
        hashMapBW16.put(3, 2);
        hashMapBW16.put(4, 18);
        hashMapBW16.put(5, 26);
        hashMapBW16.put(6, 10);
        hashMapBW16.put(7, 251);
        hashMapBW16.put(8, 250);
        hashMapBW16.put(9, 254);
        hashMapBW16.put(10, 214);
        hashMapBW16.put(11, 255);
        hashMapBW16.put(12, 107);
        hashMapBW16.put(13, 66);
        hashMapBW16.put(14, 82);
        hashMapBW16.put(15, 90);
        hashMapBW16.put(16, 74);
        hashMapBW16.put(17, 123);
        hashMapBW16.put(19, 222);
        hashMapBW16.put(20, 208);
        hashMapBW16.put(21, 248);
        hashMapBW16.put(22, 104);
        hashMapBW16.put(23, 64);
        hashMapBW16.put(24, 80);
        hashMapBW16.put(25, 88);
        hashMapBW16.put(26, 72);
        hashMapBW16.put(27, 127);
        hashMapBW16.put(28, 95);
        hashMapBW16.put(29, 223);
        hashMapBW16.put(30, 16);
        hashMapBW16.put(31, 24);
        hashMapBW16.put(32, 8);
        hashMapBW16.put(33, 0);
        hashMapBW16.put(34, 27);
        hashMapBW16.put(35, 106);
        hashMapBW16.put(36, 210);
        hashMapBW16.put(37, 30);
        hashMapBW16.put(38, 122);
        hashMapBW16.put(39, 218);
        hashMapBW16.put(42, 126);
        hashMapBW16.put(43, 219);
        hashMapBW16.put(44, 86);
        hashMapBW16.put(45, 216);
        hashMapBW16.put(46, 120);
        hashMapBW16.put(47, 75);
        hashMapBW16.put(48, 91);
        hashMapBW16.put(49, 94);
    }

    @Override
    public void create() {
        // Find all image files in the input directory
        List<Path> files = listFiles(SPLIT_TYPE.getGetDirectory());

        // Process these files
        for (Path path : files) {
            System.out.println("Working ON: " + path);

            // Load
            FileHandle fileHandle = Gdx.files.internal(path.toString());
            Pixmap pixmap = new Pixmap(fileHandle);

            final int width = SPLIT_TYPE.getTilesWide();
            final int height = SPLIT_TYPE.getTilesTall();
            final int tilesTotal = width * height;

            // Process textures
            int total = 0;
            int transparentTilesTotal = 0;
            int tilesSavedToDisk = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (hashMapBW4.get(total) == null && SPLIT_TYPE == BitWiseSplitType.FOUR
                            || hashMapBW16.get(total) == null && SPLIT_TYPE == BitWiseSplitType.SIXTEEN) {
                        total++;
                        continue;
                    }

                    Pixmap partTexture = new Pixmap(SPLIT_TYPE.tilePixelWidth, SPLIT_TYPE.tilePixelHeight, Pixmap.Format.RGBA8888);
                    partTexture.drawPixmap(
                            pixmap,
                            x * SPLIT_TYPE.tilePixelWidth,
                            y * SPLIT_TYPE.tilePixelHeight,
                            SPLIT_TYPE.tilePixelWidth,
                            SPLIT_TYPE.tilePixelHeight,
                            0,
                            0,
                            SPLIT_TYPE.tilePixelWidth,
                            SPLIT_TYPE.tilePixelHeight);

                    boolean isTransparent = isTileFullyTransparent(partTexture);

                    // Save images
                    String fileName = path.getFileName().toString().replace(PNG, ""); // String file name extension

                    // Get the file name
                    String updatedName = null;
                    switch (SPLIT_TYPE) {
                        case FOUR:
                        case FOUR_DOUBLE:
                            updatedName = IMAGE_OUTPUT_DIR + SPLIT_TYPE.getFileNamePrefix() + "-" + fileName + "-" + hashMapBW4.get(total) + PNG;
                            break;
                        case SIXTEEN:
                            updatedName = IMAGE_OUTPUT_DIR + SPLIT_TYPE.getFileNamePrefix() + "-" + fileName + "-" + hashMapBW16.get(total) + PNG;
                            break;
                    }

                    // Only save the tile file if the tile is NOT transparent
                    if (!isTransparent) {
                        PixmapIO.writePNG(new FileHandle(updatedName), partTexture);
                        tilesSavedToDisk++;
                    } else {
                        transparentTilesTotal++;
                    }

                    partTexture.dispose();
                    System.out.println("[" + (total + 1) + "/" + tilesTotal + "] Processed: " + updatedName + ", X: " + x + ", Y: " + y + ", FullyTransparent: " + isTransparent);

                    total++;
                }
            }
            System.out.println("Job done! Sprites processed total: " + total + ", TransParentTiles: " + transparentTilesTotal + ", TilesSavedToDisk: " + tilesSavedToDisk);
            pixmap.dispose();
        }

        // Pack the tiles
//        TexturePacker.process(IMAGE_OUTPUT_DIR, ATLAS_OUTPUT_DIR, "tiles");
    }

    private boolean isTileFullyTransparent(Pixmap partTexture) {
        final int numberOfPixels = partTexture.getWidth() * partTexture.getHeight();
        int numberOfTransparentPixels = 0;

        // Loop through each pixel and get that pixels alpha (color) value.
        for (int x = 0; x < partTexture.getWidth(); x++) {
            for (int y = 0; y < partTexture.getHeight(); y++) {
                Color color = valueOf(partTexture.getPixel(x, y));
                if (color.a == 0) numberOfTransparentPixels++;
            }
        }

        // If the number of pixels and the number of transparent pixels equals, return true.
        return numberOfPixels == numberOfTransparentPixels;
    }

    public static Color valueOf(int color) {
        float r = ((color >> 16) & 0xff) / 255.0f;
        float g = ((color >>  8) & 0xff) / 255.0f;
        float b = ((color      ) & 0xff) / 255.0f;
        float a = ((color >> 24) & 0xff) / 255.0f;
        return new Color(r, g, b, a);
    }

    @Override
    public void render() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {

    }

    public static List<Path> listFiles(String rootDirectory) {
        List<Path> files = new ArrayList<>();
        listFiles(rootDirectory, files);

        return files;
    }

    private static void listFiles(String path, List<Path> collectedFiles) {
        File root = new File(path);
        File[] files = root.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), collectedFiles);
            } else {
                collectedFiles.add(file.toPath());
            }
        }
    }
}
