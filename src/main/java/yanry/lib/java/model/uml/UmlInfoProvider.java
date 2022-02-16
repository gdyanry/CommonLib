package yanry.lib.java.model.uml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface UmlInfoProvider {
    /**
     * 获取指定类的uml信息，若返回null则不生成该类的类图。
     *
     * @param classType     需要生成类图的类
     * @param classRelation 需要生成类图的类与当前类的关系
     * @return 需要生成类图的类的uml信息
     */
    UmlInfo getClassUmlInfo(Class<?> classType, ClassRelation classRelation);

    UmlInfo getConstructorUmlInfo(Constructor<?> constructor);

    UmlInfo getFieldUmlInfo(Field field);

    UmlInfo getMethodUmlInfo(Method method);
}
