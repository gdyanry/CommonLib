package yanry.lib.java.model.uml;

import yanry.lib.java.interfaces.Consumer;
import yanry.lib.java.model.log.Logger;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.*;

public class ClassDiagramBuilder {
    private HashMap<Class<?>, ClassInfo> classInfoHolder = new LinkedHashMap<>();
    private UmlInfoProvider umlInfoProvider;

    public ClassDiagramBuilder(UmlInfoProvider umlInfoProvider) {
        this.umlInfoProvider = umlInfoProvider;
    }

    public static void main(String[] args) throws FileNotFoundException {
//        new ClassDiagramBuilder(new SimpleUmlInfoProvider())
//                .addClass(Processor.class)
//                .build("Processor类图", System.out);
        new ClassDiagramBuilder(new SimpleUmlInfoProvider()
//                .rejectClasses(Logger.class, LogLevel.class)
                .acceptPackages("yanry.lib.java.model.log"))
                .addClass(Logger.class)
//                .addClass(ConsoleHandler.class)
//                .addClass(SimpleFormatter.class)
                .build(null, System.out);
//        new ClassDiagramBuilder(new SimpleUmlInfoProvider()
//                .acceptClassRelations(ClassRelation.Extends, ClassRelation.Implements)
//                .acceptMemberModifiers(Modifier.PUBLIC | Modifier.ABSTRACT))
//                .addClass(StringBuilder.class)
//                .build(null, System.out);
    }

    public ClassDiagramBuilder addClass(Class<?> targetClass) {
        doAddClass(targetClass, ClassRelation.Extends);
        return this;
    }

    private ClassInfo doAddClass(Class<?> targetClass, ClassRelation classRelation) {
        if (targetClass.isPrimitive()) {
            return null;
        }
        ClassInfo classInfo = classInfoHolder.get(targetClass);
        if (classInfo == null) {
            UmlInfo umlInfo = umlInfoProvider.getClassUmlInfo(targetClass, classRelation);
            if (umlInfo != null) {
                return new ClassInfo(targetClass, umlInfo);
            }
        }
        return classInfo;
    }

    public void build(String title, OutputStream outputStream) {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("@startuml");
        writer.println("'https://plantuml.com/class-diagram");
        if (title != null) {
            writer.write("title ");
            writer.println(title);
        }
        for (Map.Entry<Class<?>, ClassInfo> entry : classInfoHolder.entrySet()) {
            ClassInfo classInfo = entry.getValue();
            writer.println(classInfo.presenter.toString());
            for (Map.Entry<Class<?>, ClassRelation> relationEntry : classInfo.entrySet()) {
                Class<?> fromClass = entry.getKey();
                Class<?> toClass = relationEntry.getKey();
                ClassRelation classRelation = relationEntry.getValue();
                if (fromClass == toClass && classRelation == ClassRelation.Navigate) {
                    continue;
                }
                writer.write(fromClass.getSimpleName());
                writer.write(' ');
                writer.write(classRelation.getSymbol());
                writer.write(' ');
                writer.println(toClass.getSimpleName());
            }
        }
        writer.println("@enduml");
        writer.flush();
    }

    private class ClassInfo extends LinkedHashMap<Class<?>, ClassRelation> {
        private StringBuilder presenter = new StringBuilder();

        public ClassInfo(Class<?> targetClass, UmlInfo umlInfo) {
            classInfoHolder.put(targetClass, this);
            // 类型
            presenter.append(System.lineSeparator());
            if (targetClass.isInterface()) {
                presenter.append("interface");
            } else if (targetClass.isEnum()) {
                presenter.append("enum");
            } else if (targetClass.isAnnotation()) {
                presenter.append("annotation");
            } else if (Modifier.isAbstract(targetClass.getModifiers())) {
                presenter.append("abstract class");
            } else {
                presenter.append("class");
            }
            presenter.append(" ").append(targetClass.getSimpleName());
            // 泛型
            TypeVariable<? extends Class<?>>[] typeParameters = targetClass.getTypeParameters();
            if (typeParameters != null && typeParameters.length > 0) {
                presenter.append('<');
                for (TypeVariable<? extends Class<?>> typeParameter : typeParameters) {
                    appendGenericType(typeParameter, ClassRelation.Navigate, true);
                    presenter.append(", ");
                }
                presenter.delete(presenter.length() - 2, presenter.length()).append('>');
            }
            // 父类
            Class<?> superclass = targetClass.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                addRelation(superclass, ClassRelation.Extends);
            }
            // 实现接口
            ClassRelation interfaceRelation = targetClass.isInterface() ? ClassRelation.Extends : ClassRelation.Implements;
            for (Class<?> anInterface : targetClass.getInterfaces()) {
                addRelation(anInterface, interfaceRelation);
            }
            presenter.append(" {");
            List<MemberNote> memberNotes = new LinkedList<>();
            Constructor<?>[] constructors = targetClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                UmlInfo constructorUmlInfo = getMemberUmlInfo(umlInfo, umlInfoProvider.getConstructorUmlInfo(constructor));
                if (constructorUmlInfo != null) {
                    appendMember(constructor.getModifiers(), targetClass.getSimpleName());
                    appendExecutable(memberNotes, constructor, constructorUmlInfo);
                }
            }
            // 字段
            Field[] declaredFields = targetClass.getDeclaredFields();
            Arrays.sort(declaredFields, Comparator.comparingInt(Field::getModifiers).thenComparing(Field::getName));
            for (Field field : declaredFields) {
                UmlInfo fieldUmlInfo = getMemberUmlInfo(umlInfo, umlInfoProvider.getFieldUmlInfo(field));
                if (fieldUmlInfo != null) {
                    appendMember(field.getModifiers(), field.getName());
                    presenter.append(": ");
                    appendGenericType(field.getGenericType(), ClassRelation.Contains, false);
                    addMemberNote(fieldUmlInfo, field, null, memberNotes);
                }
            }
            // 方法
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            Arrays.sort(declaredMethods, Comparator.comparingInt(Method::getModifiers).thenComparing(Method::getName).thenComparingInt(Method::getParameterCount));
            for (Method method : declaredMethods) {
                UmlInfo methodUmlInfo = getMemberUmlInfo(umlInfo, umlInfoProvider.getMethodUmlInfo(method));
                if (methodUmlInfo != null) {
                    appendMember(method.getModifiers(), method.getName());
                    appendExecutable(memberNotes, method, methodUmlInfo);
                    presenter.append(':');
                    appendGenericType(method.getGenericReturnType(), ClassRelation.Has, false);
                }
            }
            presenter.append(System.lineSeparator()).append('}');
            String note = umlInfo.getNote();
            if (note.length() > 0) {
                presenter.append(System.lineSeparator());
                presenter.append("note left: ").append(note);
            }
            for (MemberNote memberNote : memberNotes) {
                memberNote.accept(targetClass);
            }
        }

        private void appendExecutable(List<MemberNote> memberNotes, Executable executable, UmlInfo umlInfo) {
            int from = presenter.length();
            presenter.append('(');
            Type[] parameterTypes = executable.getGenericParameterTypes();
            if (parameterTypes.length > 0) {
                for (Type parameterType : parameterTypes) {
                    appendGenericType(parameterType, ClassRelation.Uses, false);
                    presenter.append(", ");
                }
                presenter.delete(presenter.length() - 2, presenter.length());
            }
            presenter.append(')');
            int to = presenter.length() - 2;
            addMemberNote(umlInfo, executable, presenter.subSequence(from, to), memberNotes);
        }

        private UmlInfo getMemberUmlInfo(UmlInfo typeInfo, UmlInfo memberInfo) {
            if (typeInfo.isInclude()) {
                if (memberInfo != null && memberInfo.isInclude()) {
                    return memberInfo;
                }
            } else {
                if (memberInfo == null) {
                    return UmlInfo.getDefault(true);
                }
                if (memberInfo.isInclude()) {
                    return memberInfo;
                }
            }
            return null;
        }

        private void addRelation(Class<?> type, ClassRelation relation) {
            if (doAddClass(type, relation) != null) {
                ClassRelation oldRelation = get(type);
                if (oldRelation == null || oldRelation.ordinal() > relation.ordinal()) {
                    put(type, relation);
                }
            }
        }

        private void appendMember(int modifiers, String name) {
            presenter.append(System.lineSeparator());
            if (Modifier.isPrivate(modifiers)) {
                presenter.append('-');
            } else if (Modifier.isProtected(modifiers)) {
                presenter.append('#');
            } else if (Modifier.isPublic(modifiers)) {
                presenter.append('+');
            } else {
                presenter.append('~');
            }
            if (Modifier.isAbstract(modifiers)) {
                presenter.append("{abstract}");
            } else if (Modifier.isStatic(modifiers)) {
                presenter.append("{static}");
            }
            presenter.append(name);
        }

        private void addMemberNote(UmlInfo umlInfo, Member member, CharSequence signature, List<MemberNote> memberNotes) {
            String note = umlInfo.getNote();
            if (note.length() > 0) {
                memberNotes.add(new MemberNote(member, signature, note));
            }
        }

        private void appendGenericType(Type type, ClassRelation classRelation, boolean expendTypeVariableBounds) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                appendGenericType(rawType, classRelation, expendTypeVariableBounds);
                presenter.append('<');
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    for (Type actualTypeArgument : actualTypeArguments) {
                        appendGenericType(actualTypeArgument, ClassRelation.Navigate, !classInfoHolder.containsKey(rawType));
                        presenter.append(", ");
                    }
                    presenter.delete(presenter.length() - 2, presenter.length());
                }
                presenter.append('>');
            } else if (type instanceof Class) {
                Class<?> classType = (Class<?>) type;
                presenter.append(classType.getSimpleName());
                while (classType.isArray()) {
                    classType = classType.getComponentType();
                }
                addRelation(classType, classRelation);
            } else if (type instanceof WildcardType) {
                presenter.append("?");
                WildcardType wildcardType = (WildcardType) type;
                if (!appendBounds(wildcardType.getUpperBounds(), " extends ", expendTypeVariableBounds)) {
                    appendBounds(wildcardType.getLowerBounds(), " super ", expendTypeVariableBounds);
                }
            } else if (type instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) type;
                presenter.append(typeVariable.getName());
                if (expendTypeVariableBounds) {
                    appendBounds(typeVariable.getBounds(), " extends ", true);
                }
            } else if (type instanceof GenericArrayType) {
                appendGenericType(((GenericArrayType) type).getGenericComponentType(), classRelation, expendTypeVariableBounds);
                presenter.append("[]");
            } else {
                Logger.getDefault().ee("unhandled type: ", type.getClass());
                presenter.append(type.getTypeName());
            }
        }

        private boolean appendBounds(Type[] bounds, String joint, boolean expendTypeVariableBounds) {
            if (bounds.length > 0) {
                if (bounds.length == 1 && bounds[0] == Object.class) {
                    return false;
                }
                presenter.append(joint);
                for (Type bound : bounds) {
                    appendGenericType(bound, ClassRelation.Navigate, expendTypeVariableBounds);
                    presenter.append(", ");
                }
                presenter.delete(presenter.length() - 2, presenter.length());
                return true;
            }
            return false;
        }

        private class MemberNote implements Consumer<Class<?>> {
            private Member member;
            private CharSequence signature;
            private String note;

            MemberNote(Member member, CharSequence signature, String note) {
                this.member = member;
                this.signature = signature;
                this.note = note;
            }

            @Override
            public void accept(Class<?> type) {
                presenter.append(System.lineSeparator()).append("note left of ").append(type.getSimpleName()).append("::");
                if (signature != null) {
                    presenter.append('"');
                }
                presenter.append(member.getName());
                if (signature != null) {
                    presenter.append(signature);
                    presenter.append('"');
                }
                presenter.append(System.lineSeparator()).append(note).append(System.lineSeparator()).append("end note");
            }
        }
    }
}
