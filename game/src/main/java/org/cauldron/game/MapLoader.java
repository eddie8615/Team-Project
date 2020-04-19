package org.cauldron.game;

import org.joml.Vector2d;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapLoader {

    private static final String mapPath = System.getProperty("user.home") + "/Games/Blast/";
    private static Map<String, List<Vector2d>> mapNameToPoints = new HashMap<>();
    private static Terrain terrain = null;

    public static void init() {
        list();
    }

    public static List<String> list() {
        mapNameToPoints = new HashMap<>();
        List<String> names = new ArrayList<>();
        readRes("game/maps/default.map");

        try (Stream<Path> walk = Files.walk(Paths.get(mapPath))) {

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".map")).collect(Collectors.toList());

            for (String path : result) {
                read(path);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String name : mapNameToPoints.keySet()) {
            names.add(name);
        }

        return names;
    }

    private static void readRes(String path) {
        String name = null;
        ArrayList<Vector2d> controlPoints = new ArrayList<Vector2d>();
        // This will reference one line at a time
        String line = null;

        try {
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(path)));

            if ((line = bufferedReader.readLine()) != null) {
                name = line;
            }

            while ((line = bufferedReader.readLine()) != null) {
                int[] xyz = Arrays.stream(line.split(" ")).mapToInt(Integer::parseInt).toArray();
                controlPoints.add(new Vector2d(xyz[0], xyz[1]));
            }

            // Always close files.
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            path + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + path + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }

        if (name == null)
            return;
        mapNameToPoints.put(name, controlPoints);
    }

    private static void read(String path) {
        String name = null;
        ArrayList<Vector2d> controlPoints = new ArrayList<Vector2d>();

        try {
            File f = new File(path);
            Scanner reader = new Scanner(f);
            while (reader.hasNextLine()) {
                if (name == null) {
                    name = reader.nextLine();
                } else {
                    String line = reader.nextLine();
                    int[] xyz = Arrays.stream(line.split(" ")).mapToInt(Integer::parseInt).toArray();
                    controlPoints.add(new Vector2d(xyz[0], xyz[1]));
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        if (name == null)
            return;
        mapNameToPoints.put(name, controlPoints);
    }

    public static void load(Terrain t, String name) {
        terrain = t;
        t.loadMap((ArrayList<Vector2d>) mapNameToPoints.get(name));
    }

    public static void setTerrain(Terrain t) {
        terrain = t;
    }

    public static void load(String name) {
        if (terrain == null)
            return;
        terrain.loadMap((ArrayList<Vector2d>) mapNameToPoints.get(name));
    }

    private static boolean dirCheck() {
        if (!Files.exists(Paths.get(mapPath))) {
            try {
                Files.createDirectories(Paths.get(mapPath));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static String convertNameToFilename(String name) {
        name = name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".map";
        return name;
    }

    public static boolean save(String name, ArrayList<Vector2d> controlPoints) {
        if (mapNameToPoints.containsKey(name)) {
            System.out.println("Map already exists.");
            return false;
        }

        if (!dirCheck()) {
            System.out.println("Directory creation failed.");
            return false;
        }

        String filename = convertNameToFilename(name);

        try {
            FileWriter writer = new FileWriter(mapPath + filename);
            writer.write(name + "\n");
            for (Vector2d v : controlPoints) {
                if (v == null)
                    continue;
                writer.write(((int) v.x) + " " + ((int) v.y) + "\n");
            }
            writer.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred in writing to the file.");
            e.printStackTrace();
        }
        return true;
    }

    public static boolean delete(String name) {
        try (Stream<Path> walk = Files.walk(Paths.get(mapPath))) {

            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            for (String path : result) {
                if (name.equals(Files.readAllLines(Paths.get(path)).get(0))) {
                    new File(path).delete();
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
