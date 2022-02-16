package yanry.lib.java.model.uml;

import java.lang.reflect.*;
import java.util.EnumSet;
import java.util.HashSet;

public class SimpleUmlInfoProvider implements UmlInfoProvider {
    private int acceptedModifiers = Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.PROTECTED;
    private String[] acceptedPackages;
    private EnumSet<ClassRelation> acceptedRelations = EnumSet.allOf(ClassRelation.class);
    private HashSet<Class<?>> rejectedClasses = new HashSet<>();

    public SimpleUmlInfoProvider acceptMemberModifiers(int modifiers) {
        acceptedModifiers = modifiers;
        return this;
    }

    public SimpleUmlInfoProvider acceptPackages(String... packages) {
        acceptedPackages = packages;
        return this;
    }

    public SimpleUmlInfoProvider acceptClassRelations(ClassRelation... classRelations) {
        acceptedRelations = EnumSet.noneOf(ClassRelation.class);
        for (ClassRelation classRelation : classRelations) {
            acceptedRelations.add(classRelation);
        }
        return this;
    }

    public SimpleUmlInfoProvider rejectClasses(Class<?>... classes) {
        for (Class<?> classType : classes) {
            rejectedClasses.add(classType);
        }
        return this;
    }

    @Override
    public UmlInfo getClassUmlInfo(Class<?> classType, ClassRelation classRelation) {
        if (rejectedClasses.contains(classType)) {
            return null;
        }
        UmlInfo umlInfo = UmlInfo.get(classType);
        if (umlInfo != null) {
            return umlInfo;
        }
        if (acceptedRelations.contains(classRelation)) {
            if (acceptedPackages != null) {
                String packageName = classType.getPackage().getName();
                for (String acceptedPackage : acceptedPackages) {
                    if (packageName.startsWith(acceptedPackage)) {
                        return UmlInfo.getDefault(true);
                    }
                }
            } else {
                return UmlInfo.getDefault(true);
            }
        }
        return null;
    }

    @Override
    public UmlInfo getConstructorUmlInfo(Constructor<?> constructor) {
        UmlInfo umlInfo = UmlInfo.get(constructor);
        if (umlInfo != null) {
            return umlInfo;
        }
        return acceptMember(constructor) ? UmlInfo.getDefault(true) : null;
    }

    @Override
    public UmlInfo getFieldUmlInfo(Field field) {
        UmlInfo umlInfo = UmlInfo.get(field);
        if (umlInfo != null) {
            return umlInfo;
        }
        return acceptMember(field) ? UmlInfo.getDefault(true) : null;
    }

    @Override
    public UmlInfo getMethodUmlInfo(Method method) {
        UmlInfo umlInfo = UmlInfo.get(method);
        if (umlInfo != null) {
            return umlInfo;
        }
        return acceptMember(method) ? UmlInfo.getDefault(true) : null;
    }

    private boolean acceptMember(Member member) {
        return (acceptedModifiers & member.getModifiers()) > 0;
    }
}
