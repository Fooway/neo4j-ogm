package org.neo4j.ogm.strategy.annotated;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.strategy.simple.SimpleClassDictionary;

import java.util.List;

public class AnnotatedClassDictionary extends SimpleClassDictionary {

    public AnnotatedClassDictionary(String... packages) {
        super(packages);
    }

    @Override
    public Class match(String label) {
        String annotation = Label.class.getName();
        List<ClassInfo> classInfos = domainInfo().getClassInfosWithAnnotation(annotation);
        if (classInfos != null) {
            for (ClassInfo classInfo : classInfos) {
                try {
                    Class clazz=Class.forName(classInfo.toString());
                    Label labelClass = (Label) clazz.getAnnotation(Label.class);
                    if (labelClass.name() == null) {
                        if (clazz.getSimpleName().equals(label)) {
                            return clazz;
                        }
                    } else {
                        if (labelClass.name().equals(label)) {
                            return clazz;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.match(label);
    }
}