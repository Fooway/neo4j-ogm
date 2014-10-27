package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.metadata.ClassPathScanner;
import org.neo4j.ogm.metadata.MappingException;

import java.io.*;
import java.util.*;

public class DomainInfo implements ClassInfoProcessor {

    private List<String> classPaths = new ArrayList<>();

    private final HashMap<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final HashMap<String, InterfaceInfo> interfaceNameToInterfaceInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
    private final HashMap<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

    public DomainInfo(String... packages) {
        load(packages);
    }

    private void buildAnnotationNameToClassInfoMap() {
        // A <-[:has_annotation]- T
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            for (AnnotationInfo annotation : classInfo.annotations()) {
                ArrayList<ClassInfo> classInfoList = annotationNameToClassInfo.get(annotation.getName());
                if (classInfoList == null) {
                    annotationNameToClassInfo.put(annotation.getName(), classInfoList = new ArrayList<>());
                }
                classInfoList.add(classInfo);
            }
        }
    }

    private void buildInterfaceHierarchy() {
        // I - [:extends] -> J
        for (InterfaceInfo interfaceInfo : interfaceNameToInterfaceInfo.values()) {
            constructInterfaceHierarcy(interfaceInfo);
        }
    }

    private void constructInterfaceHierarcy(InterfaceInfo interfaceInfo) {
        if (interfaceInfo.allSuperInterfaces().isEmpty() && !interfaceInfo.superInterfaces().isEmpty()) {
            interfaceInfo.allSuperInterfaces().addAll(interfaceInfo.superInterfaces());
            for (InterfaceInfo superinterfaceInfo : interfaceInfo.superInterfaces()) {
                if (superinterfaceInfo != null) {
                    constructInterfaceHierarcy(superinterfaceInfo);
                    interfaceInfo.allSuperInterfaces().addAll(superinterfaceInfo.allSuperInterfaces());
                }
            }
        }
    }

    private void buildInterfaceNameToClassInfoMap() {
        // T -[:implements]-> I
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            HashSet<InterfaceInfo> interfaceAndSuperinterfaces = new HashSet<>();
            for (InterfaceInfo interfaceInfo : classInfo.interfaces()) {
                interfaceAndSuperinterfaces.add(interfaceInfo);
                if (interfaceInfo != null) {
                    interfaceAndSuperinterfaces.addAll(interfaceInfo.allSuperInterfaces());
                }
            }
            for (InterfaceInfo interfaceInfo : interfaceAndSuperinterfaces) {
                ArrayList<ClassInfo> classInfoList = interfaceNameToClassInfo.get(interfaceInfo.name());
                if (classInfoList == null) {
                    interfaceNameToClassInfo.put(interfaceInfo.name(), classInfoList = new ArrayList<>());
                }
                classInfoList.add(classInfo);
            }
        }
    }

    public void buildTransitiveInterfaceImplementations() {
        // transitive interface implementations: S-[:extends]->T-[:implements]->I  => S-[:implements]->I
        for (String interfaceName : interfaceNameToClassInfo.keySet()) {
            ArrayList<ClassInfo> classes = interfaceNameToClassInfo.get(interfaceName);
            HashSet<ClassInfo> subClasses = new HashSet<>(classes);
            for (ClassInfo classInfo : classes) {
                if (classInfo != null) {
                    for (ClassInfo subClassInfo : classInfo.directSubclasses()) {
                        subClasses.add(subClassInfo);
                    }
                }
            }
            interfaceNameToClassInfo.put(interfaceName, new ArrayList<>(subClasses));
        }
    }

    public void finish() {

        if (classNameToClassInfo.isEmpty() && interfaceNameToInterfaceInfo.isEmpty()) {
            return;
        }

        buildAnnotationNameToClassInfoMap();
        buildInterfaceHierarchy();
        buildInterfaceNameToClassInfoMap();
        buildTransitiveInterfaceImplementations();

        // TODO: transitive annotations
        // if a superclass type, method or field is annotated, inject the annotation to subclasses
        // explicitly. Saves having to walk through type hierarchies to find an annotation.
        // must also include annotated interfaces.  WHICH WE DONT DO YET.

    }

    public void process(final InputStream inputStream) throws IOException {

        ClassInfo classInfo = new ClassInfo(inputStream);
        String className = classInfo.name();
        String superclassName = classInfo.superclassName();

        if (className != null) {
            if (classInfo.isInterface()) {
                InterfaceInfo thisInterfaceInfo = interfaceNameToInterfaceInfo.get(className);
                if (thisInterfaceInfo == null) {
                    interfaceNameToInterfaceInfo.put(className, new InterfaceInfo(className));
                }
            } else {
                ClassInfo thisClassInfo = classNameToClassInfo.get(className);
                if (thisClassInfo == null) {
                    thisClassInfo = classInfo;
                    classNameToClassInfo.put(className, thisClassInfo);
                }
                if (!thisClassInfo.hydrated()) {
                    thisClassInfo.hydrate(classInfo);
                    ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
                    if (superclassInfo == null) {
                        classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
                    } else {
                        superclassInfo.addSubclass(thisClassInfo);
                    }
                }
            }
        }

    }

    private void load(String... packages) {

        classPaths.clear();
        classNameToClassInfo.clear();
        interfaceNameToInterfaceInfo.clear();
        annotationNameToClassInfo.clear();
        interfaceNameToClassInfo.clear();

        for (String packageName : packages) {
            String path = packageName.replaceAll("\\.", File.separator);
            classPaths.add(path);
        }

        new ClassPathScanner().scan(classPaths, this);

    }

    public ClassInfo getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    public ClassInfo getClassSimpleName(String simpleClassName) {

        ClassInfo match = null;
        for (String fqn : classNameToClassInfo.keySet()) {
            if (fqn.endsWith("." + simpleClassName)) {
                if (match == null) {
                    match = classNameToClassInfo.get(fqn);
                } else {
                    throw new MappingException("More than one class has simple name: " + simpleClassName);
                }
            }
        }
        return match;
    }

    public ClassInfo getNamedClassWithAnnotation(String annotation, String className) {
        for (ClassInfo classInfo : annotationNameToClassInfo.get(annotation)) {
            if (classInfo.name().equals(className)) {
                return classInfo;
            }
        }
        return null;
    }

    public List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
        return annotationNameToClassInfo.get(annotation);
    }
}
