package runtime;

import semanticanalysis.types.DSLType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a method to parse all components in game/src/contrib and game/src/core if any
 * part of it is annotated with the @DSLType Annotation
 */
public class ComponentParser {

    String contribPath = "/home/lukas/Documents/Arbeit_Dungeon/Dungeon/game/src/contrib";
    String corePath = "/home/lukas/Documents/Arbeit_Dungeon/Dungeon/game/src/core";
    String annotationName = "DSLType";
    List<File> foundFiles = new ArrayList<>();
    List<Class<?>> foundClasses = new ArrayList<>();

    /**
     * parses the contrib and core direcotries for all @DSLType annotated classes
     *
     * @return List of all found classes
     */
    public List<Class<?>> parseComponents() {
        File contribDirectory = new File(contribPath);
        File coreDirectory = new File(corePath);

        findFiles(contribDirectory);
        findFiles(coreDirectory);

        loadClasses();

        return foundClasses;
    }

    private void findFiles(File file) {
        // check if param directory exists and is a directory
        if (file.exists() && file.isDirectory()) {

            // File[] files = file.listFiles((dir, name) -> name.endsWith(".java"));
            File[] files = file.listFiles();
            if (files != null) {
                for (File newFile : files) {
                    if (newFile.isDirectory()) {
                        findFiles(newFile);
                    } else if (newFile.isFile() && newFile.getName().endsWith(".java")) {
                        if (checkForDSLType(newFile)) {
                            foundFiles.add(newFile);
                        }
                    }
                }
            }
        }
    }

    private boolean checkForDSLType(File file) {
        String code = readCodeFromFile(file);
        assert code != null;
        return code.contains(annotationName);
    }

    private void loadClasses() {
        if (!foundFiles.isEmpty()) {
            for (File file : foundFiles) {
                try {

                    String fullQualifiedName = getFullyQualifiedName(readCodeFromFile(file));
                    Class<?> foundClass = Class.forName(fullQualifiedName);

                    if (foundClass.isAnnotationPresent(DSLType.class)) {
                        foundClasses.add(foundClass);
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String readCodeFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder codelines = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                codelines.append(line).append(System.lineSeparator());
            }

            return codelines.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFullyQualifiedName(String code) {
        String packageName = extractPackageName(code);
        String className = extractClassName(code);

        if (packageName != null && className != null) {
            return packageName + "." + className;
        }

        return null;
    }

    private String extractPackageName(String code) {
        Pattern pattern = Pattern.compile("package\\s+([\\w.]+);");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("class\\s+([\\w$]+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
