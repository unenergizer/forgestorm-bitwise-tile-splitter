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

    private static final BitWiseSplitType SPLIT_TYPE = BitWiseSplitType.SIXTEEN_MTN_WALL;

    @Getter
    @AllArgsConstructor
    public enum BitWiseSplitType {
        FOUR_ORIGINAL("BW4", "input/BW16/", 4, 4, 16, 16),
        FOUR_DOUBLE("BW4", "input/BW16H32/", 4, 4, 16, 32),

        SIXTEEN_ORIGINAL("BW16", "input/BW48/", 10, 5, 16, 16),
        SIXTEEN_MTN_WALL("BW16", "input/BW48MTN/", 5, 3, 16, 16);

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

    private final HashMap<Integer, Integer> bitWise16Full;
    private final HashMap<Integer, Integer> bitWise48Full;
    private final HashMap<Integer, Integer> bitWise48MtnWall;

    public SplitterMain() {
        // Original full 16
        bitWise16Full = new HashMap<>();
        // Row 1
        bitWise16Full.put(0, 5);
        bitWise16Full.put(1, 7);
        bitWise16Full.put(2, 3);
        bitWise16Full.put(3, 1);

        // Row 2
        bitWise16Full.put(4, 13);
        bitWise16Full.put(5, 15);
        bitWise16Full.put(6, 11);
        bitWise16Full.put(7, 9);

        // Row 3
        bitWise16Full.put(8, 12);
        bitWise16Full.put(9, 14);
        bitWise16Full.put(10, 10);
        bitWise16Full.put(11, 8);

        // Row 4
        bitWise16Full.put(12, 4);
        bitWise16Full.put(13, 6);
        bitWise16Full.put(14, 2);
        bitWise16Full.put(15, 0);

        // Original full 48
        bitWise48Full = new HashMap<>();
        // Row 1
        bitWise48Full.put(0, 22);
        bitWise48Full.put(1, 31);
        bitWise48Full.put(2, 11);
        bitWise48Full.put(3, 2);
        bitWise48Full.put(4, 18);
        bitWise48Full.put(5, 26);
        bitWise48Full.put(6, 10);
        bitWise48Full.put(7, 251);
        bitWise48Full.put(8, 250);
        bitWise48Full.put(9, 254);

        // Row 2
        bitWise48Full.put(10, 214);
        bitWise48Full.put(11, 255);
        bitWise48Full.put(12, 107);
        bitWise48Full.put(13, 66);
        bitWise48Full.put(14, 82);
        bitWise48Full.put(15, 90);
        bitWise48Full.put(16, 74);
        bitWise48Full.put(17, 123);
        // Skip 18 it is blank
        bitWise48Full.put(19, 222);

        // Row 3
        bitWise48Full.put(20, 208);
        bitWise48Full.put(21, 248);
        bitWise48Full.put(22, 104);
        bitWise48Full.put(23, 64);
        bitWise48Full.put(24, 80);
        bitWise48Full.put(25, 88);
        bitWise48Full.put(26, 72);
        bitWise48Full.put(27, 127);
        bitWise48Full.put(28, 95);
        bitWise48Full.put(29, 223);

        // Row 4
        bitWise48Full.put(30, 16);
        bitWise48Full.put(31, 24);
        bitWise48Full.put(32, 8);
        bitWise48Full.put(33, 0);
        bitWise48Full.put(34, 27);
        bitWise48Full.put(35, 106);
        bitWise48Full.put(36, 210);
        bitWise48Full.put(37, 30);
        bitWise48Full.put(38, 122);
        bitWise48Full.put(39, 218);

        // Row 5
        // Skip 40 it is blank
        // Skip 41 it is blank
        bitWise48Full.put(42, 126);
        bitWise48Full.put(43, 219);
        bitWise48Full.put(44, 86);
        bitWise48Full.put(45, 216);
        bitWise48Full.put(46, 120);
        bitWise48Full.put(47, 75);
        bitWise48Full.put(48, 91);
        bitWise48Full.put(49, 94);


        // Modified bitwise 48 for compact mountain wall
        bitWise48MtnWall = new HashMap<>();
        // Row 1
        bitWise48MtnWall.put(0, 22);
        bitWise48MtnWall.put(1, 31);
        bitWise48MtnWall.put(2, 11);
        bitWise48MtnWall.put(3, 251);
        bitWise48MtnWall.put(4, 254);

        // Row 2
        bitWise48MtnWall.put(5, 214);
        bitWise48MtnWall.put(6, 255);
        bitWise48MtnWall.put(7, 107);
        bitWise48MtnWall.put(8, 127);
        bitWise48MtnWall.put(9, 223);

        // Row 3
        bitWise48MtnWall.put(10, 208);
        bitWise48MtnWall.put(11, 248);
        bitWise48MtnWall.put(12, 104);
        bitWise48MtnWall.put(13, 126);
        bitWise48MtnWall.put(14, 219);
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
                    if (bitWise16Full.get(total) == null && SPLIT_TYPE == BitWiseSplitType.FOUR_ORIGINAL
                            || bitWise48Full.get(total) == null && SPLIT_TYPE == BitWiseSplitType.SIXTEEN_ORIGINAL) {
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
                        case FOUR_ORIGINAL:
                        case FOUR_DOUBLE:
                            updatedName = IMAGE_OUTPUT_DIR + SPLIT_TYPE.getFileNamePrefix() + "-" + fileName + "-" + bitWise16Full.get(total) + PNG;
                            break;
                        case SIXTEEN_ORIGINAL:
                            updatedName = IMAGE_OUTPUT_DIR + SPLIT_TYPE.getFileNamePrefix() + "-" + fileName + "-" + bitWise48Full.get(total) + PNG;
                            break;
                        case SIXTEEN_MTN_WALL:
                            updatedName = IMAGE_OUTPUT_DIR + SPLIT_TYPE.getFileNamePrefix() + "-" + fileName + "-" + bitWise48MtnWall.get(total) + PNG;
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
            System.out.println("Job done! Sprites processed total: " + total + ", TransparentTiles: " + transparentTilesTotal + ", TilesSavedToDisk: " + tilesSavedToDisk);
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
