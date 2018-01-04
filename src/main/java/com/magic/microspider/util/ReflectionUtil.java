package com.magic.microspider.util;

import com.google.common.base.Predicate;
import org.apache.commons.beanutils.PropertyUtils;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * Created by liunn on 2018/1/4.
 */
public class ReflectionUtil {

    private static Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
    }


    /**
     * 获取某个类的所有属性
     * @param clazz
     * @return key：属性名，value：属性对象
     * @see [类、类#方法、类#成员]
     */
    public static Map<String, Field> getAllFields(Class clazz, Class exceptSuper)
    {
        Map<String, Field> FIELD_MAP = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        Set<Field> allFields = new HashSet<Field>();
        for (Field field : fields)
        {
            allFields.add(field);
        }
        getSuperFields(allFields, clazz, exceptSuper);
        for (Field field : allFields)
        {
            String fieldName = field.getName();
            FIELD_MAP.put(fieldName, field);
        }
        return FIELD_MAP;
    }

    /**
     * 获取某个类的所有方法
     * @param clazz
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static Set<Method> getAllMethods(Class clazz, Class exceptSuper)
    {
        Method[] methods = clazz.getDeclaredMethods();
        Set<Method> allMethods = new HashSet<Method>();
        for (Method method : methods)
        {
            allMethods.add(method);
        }
        getSuperMethods(allMethods, clazz, exceptSuper);
        return allMethods;
    }

    private static void getSuperFields(Set<Field> allFields, Class clazz, Class exceptSuper)
    {
        Class superClazz = clazz.getSuperclass();
        if (null != superClazz)
        {
            if (null != exceptSuper && superClazz.equals(exceptSuper))
            {
                return;
            }
            Field[] superFields = superClazz.getDeclaredFields();
            if (null != superFields && superFields.length > 0)
            {
                for (Field superField : superFields)
                {
                    allFields.add(superField);
                }
            }
            getSuperFields(allFields, superClazz, exceptSuper);
        }
    }

    private static void getSuperMethods(Set<Method> allMethods, Class clazz, Class exceptSuper)
    {
        Class superClazz = clazz.getSuperclass();
        if (null != superClazz)
        {
            if (null != exceptSuper && superClazz.equals(exceptSuper))
            {
                return;
            }
            Method[] superMethods = superClazz.getDeclaredMethods();
            if (null != superMethods && superMethods.length > 0)
            {
                for (Method superMethod : superMethods)
                {
                    allMethods.add(superMethod);
                }
            }
            getSuperMethods(allMethods, superClazz, exceptSuper);
        }
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
     */
    public static Object getFieldValue(final Object object, final String fieldName)
    {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        Object result = null;
        try
        {
            result = field.get(object);
        }
        catch (IllegalAccessException e)
        {
            logger.error("不可能抛出的异常{}", e.getMessage());
        }
        return result;
    }

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value)
    {
        Field field = getDeclaredField(object, fieldName);

        if (field == null)
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");

        makeAccessible(field);

        try
        {
            field.set(object, value);
        }
        catch (IllegalAccessException e)
        {
            logger.error("不可能抛出的异常:{}", e.getMessage());
        }
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    public static Field getDeclaredField(final Object object, final String fieldName)
    {
        return getDeclaredField(object.getClass(), fieldName);
    }

    /**
     * 循环向上转型,获取类的DeclaredField.
     */
    protected static Field getDeclaredField(final Class clazz, final String fieldName)
    {
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass())
        {
            try
            {
                return superClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
    }

    /**
     * 强制转换fileld可访问.
     */
    protected static void makeAccessible(final Field field)
    {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()))
        {
            field.setAccessible(true);
        }
    }

    /**
     * 通过反射,获得定义Class时声明的父类的泛型参数的类型. 如public UserDao extends HibernateDao<User>
     *
     * @param clazz
     *            The class to introspect
     * @return the first generic declaration, or Object.class if cannot be
     *         determined
     */
    public static Class getSuperClassGenricType(final Class clazz)
    {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射,获得定义Class时声明的父类的泛型参数的类型. 如public UserDao extends
     * HibernateDao<User,Long>
     *
     * @param clazz
     *            clazz The class to introspect
     * @param index
     *            the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     *         determined
     */

    public static Class getSuperClassGenricType(final Class clazz, final int index)
    {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType))
        {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0)
        {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class))
        {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }
        return (Class) params[index];
    }

    /**
     * 提取集合中的对象的属性,组合成List.
     *
     * @param collection
     *            来源集合.
     * @param propertityName
     *            要提取的属性名.
     */
    @SuppressWarnings("unchecked")
    public static List fetchElementPropertyToList(final Collection collection, final String propertyName) throws Exception
    {

        List list = new ArrayList();

        for (Object obj : collection)
        {
            list.add(PropertyUtils.getProperty(obj, propertyName));
        }

        return list;
    }

    /**
     * 提取集合中的对象的属性,组合成由分割符分隔的字符串.
     *
     * @param collection
     *            来源集合.
     * @param propertityName
     *            要提取的属性名.
     * @param separator
     *            分隔符.
     */
    @SuppressWarnings("unchecked")
    public static String fetchElementPropertyToString(final Collection collection, final String propertyName, final String separator) throws Exception
    {
        List list = fetchElementPropertyToList(collection, propertyName);
        return CommonUtil.join(list, separator);
    }

    public static Map<String, Object[]> compareObject(Object o1, Object o2)
    {
        HashMap<String, Object[]> map = new HashMap<String, Object[]>();
        if (!o1.getClass().equals(o2.getClass()))
        {
            return map;
        }
        try
        {
            for (Field f : o1.getClass().getDeclaredFields())
            {
                f.setAccessible(true);
                Object v1 = f.get(o1);
                Object v2 = f.get(o2);
                if (v1 == null && v2 == null)
                {
                    continue;
                }
                else if (v1 != null && v2 != null)
                {
                    if (v1.getClass() == String.class && v1.toString().trim().equals(v2.toString().trim()))
                    {
                        continue;
                    }
                    else if (v1.equals(v2))
                    {
                        continue;
                    }
                }
                else if (v1 != null && v2 == null)
                {
                    if (v1.getClass() == String.class && v1.equals(""))
                    {
                        continue;
                    }
                }
                else if (v2 != null && v1 == null)
                {
                    if (v2.getClass() == String.class && v2.equals(""))
                    {
                        continue;
                    }
                }
                if (!f.getName().equalsIgnoreCase("OPERATE_DATE") && !f.getName().equalsIgnoreCase("OPERATOR") && !f.getName().equalsIgnoreCase("AD_CLIENT_ID")
                        && !f.getName().equalsIgnoreCase("AD_ORG_ID") && !f.getName().equalsIgnoreCase("CREATED") && !f.getName().equalsIgnoreCase("UPDATED") && !f.getName().equalsIgnoreCase("CREATEDBY")
                        && !f.getName().equalsIgnoreCase("UPDATEDBY") && !f.getName().equalsIgnoreCase("IS_ACTIVE"))
                    map.put(f.getName().toUpperCase(), new Object[] { f.get(o1), f.get(o2) });
                logger.info("变动属性：" + f.getName() + " " + f.get(o1) + " " + f.get(o2));
            }
            return map;
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 通过扫描，获取反射对象
     */
    public static Reflections getReflection(List<String> packNameList) {

        //
        // filter
        //
        FilterBuilder filterBuilder = new FilterBuilder();

        for (String packName : packNameList) {
            filterBuilder = filterBuilder.includePackage(packName);
        }
        Predicate<String> filter = filterBuilder;

        //
        // urls
        //
        Collection<URL> urlTotals = new ArrayList<URL>();
        for (String packName : packNameList) {
            Collection<URL> urls = ClasspathHelper.forPackage(packName);
            urlTotals.addAll(urls);
        }

        //
        Reflections reflections = new Reflections(new ConfigurationBuilder().filterInputsBy(filter)
                .setScanners(new SubTypesScanner().filterResultsBy(filter),
                        new TypeAnnotationsScanner(),
                        //                                .filterResultsBy(filter),
                        new FieldAnnotationsScanner()
                                .filterResultsBy(filter),
                        new MethodAnnotationsScanner()
                                .filterResultsBy(filter),
                        new MethodParameterScanner()).setUrls(urlTotals));

        return reflections;
    }

    public static <T> T covertNullToEmptyStr(T obj)
    {
        try
        {
            Field[] fields = obj.getClass().getDeclaredFields();

            for (Field field : fields)
            {
                if (field.getType().equals(String.class) && null == getFieldValue(obj, field.getName()))
                {
                    setFieldValue(obj, field.getName(), "");
                }
            }
        }
        catch (Exception e)
        {
        }

        return obj;
    }
}
