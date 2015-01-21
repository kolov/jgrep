package com.akolov.jgrep;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Jgrep {

    private Collection<File> allFiles;
    private Collection<File> javaFiles;
    private Collection<File> resultFiles;
    private List<String> conditionPackages = new ArrayList<>();
    private List<String> conditionImplements = new ArrayList<>();

    public Jgrep(String dirname,
                 String implementz,
                 String fileImplements,
                 String packages) throws IOException {
        File dir = new File(dirname);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Can't open folder " + dir);
        }

        allFiles = listAllFiles(dir);
        javaFiles = allFiles.stream()
            .filter(f -> f.getAbsolutePath().endsWith(".java"))
            .collect(Collectors
                .toList());
        parseCommaSeparatedToCollection(implementz, conditionImplements);
        parseCommaSeparatedToCollection(packages, conditionPackages);

        parseArgumentsFileToCollection(fileImplements, conditionImplements);
    }

    private void parseArgumentsFileToCollection(String argumentsFilename, Collection destCollection) throws IOException {
        if (argumentsFilename != null) {
            File f = new File(argumentsFilename);
            BufferedReader rdr = Files.newBufferedReader(f.toPath());
            String line;
            while ((line = rdr.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.length() > 0) {
                    destCollection.add(trimmed);
                }
            }
        }
    }

    private void parseCommaSeparatedToCollection(String implementz, Collection destCollection) {
        if (implementz != null) {
            String[] parts = implementz.split(",");
            for (int i = 0; i < parts.length; i++) {
                destCollection.add(parts[i].trim());
            }
        }
    }

    public void run() {

        System.out.println("About to scan " + javaFiles.size() + " Java files (from " + allFiles.size() + " files " +
            "total)");
        if (conditionsPresent()) {
            System.out.println("Searching matches for implements:");
            for (String className : conditionImplements) {
                System.out.println(className);
            }
            resultFiles = javaFiles.stream()
                .filter(f -> checkPackage(f))
                .filter(f -> checkImplements(f))
                .collect(Collectors.toList());
        }
        System.out.println("Found matches: " + resultFiles.size());
        for (File resultFile : resultFiles) {
            System.out.println(resultFile);
        }
    }

    private boolean checkPackage(File f) {
        if (conditionPackages.size() == 0) {
            return true;
        }
        CompilationUnit compilationUnit = parseJavaFile(f);
        return conditionPackages.stream().anyMatch(packagePrefix -> compilationUnit.getPackage().getName()
            .toString().startsWith(packagePrefix));
    }

    private boolean conditionsPresent() {
        return conditionImplements.size() > 0 || conditionPackages.size() > 0;
    }

    private boolean checkImplements(File f) {
        if (conditionImplements.size() == 0) {
            return true;
        }
        CompilationUnit compilationUnit = parseJavaFile(f);


        List<ImportDeclaration> imports = compilationUnit.getImports();
        List<TypeDeclaration> types = compilationUnit.getTypes();

        if (types == null) {
            // Java file without Compilation Unit: e.g. package-info.java
            return false;
        }
        return types.stream().anyMatch(typeDefinition -> {
            if (!ClassOrInterfaceDeclaration.class.isAssignableFrom(typeDefinition.getClass())) {
                return false;
            }

            ClassOrInterfaceDeclaration coid = (ClassOrInterfaceDeclaration) typeDefinition;
            boolean matchFound = matchesClasses(imports, coid.getExtends());
            if (!matchFound) {
                matchFound = matchesClasses(imports, coid.getImplements());
            }
            return matchFound;
        });
    }

    private CompilationUnit parseJavaFile(File f) {
        CompilationUnit compilationUnit = null;
        try {
            compilationUnit = JavaParser.parse(f);
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
        return compilationUnit;
    }

    private boolean matchesClasses(List<ImportDeclaration> imports, List<ClassOrInterfaceType> extendedClasses) {

        if (extendedClasses != null && imports != null) {
            return extendedClasses.stream()
                .anyMatch(extendedClassName ->
                        imports.stream()
                            .map(importDef -> importDef.getName().toString())
                            .filter(importedClassName ->
                                importedClassName.endsWith("." + extendedClassName.getName()))
                            .anyMatch(importedClassName -> conditionImplements.contains(importedClassName))
                );

        }

        return false;
    }

    private Collection<File> listAllFiles(File dir) {
        return addDir(dir, new ArrayList<File>());
    }

    private Collection<File> addDir(File dir, Collection<File> list) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], list);
            } else {
                list.add(files[i]);
            }
        }
        return list;
    }

    public Collection<File> getAllFiles() {
        return allFiles;
    }


}
