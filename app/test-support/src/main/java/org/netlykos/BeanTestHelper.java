package org.netlykos;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeanTestHelper {

  private static final SecureRandom SECURE_RANDOM = getSecureRandom();
  private static final Map<Class<?>, Supplier<Object>> SUPPLIER_CONSTRUCTORS = getSupplierConstructors();

  private BeanTestHelper() {
    /* do nothing constructor */
  }

  private static final Logger LOGGER = LogManager.getLogger(BeanTestHelper.class);

  public static void testPackage(String packageName) {
    Set<Class<?>> classesInPackage = findAllClassesUsingClassLoader(packageName);
    LOGGER.trace("Identified {} beans in package {}", classesInPackage.size(), packageName);
    for (Class<?> classz : classesInPackage) {
      if (classz.isRecord()) {
        testRecordClass(classz);
      }
    }
  }

  static Set<Class<?>> findAllClassesUsingClassLoader(String packageName) {
    InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    return reader.lines()
        .filter(line -> line.endsWith(".class"))
        .map(line -> getClass(line, packageName))
        .collect(Collectors.toSet());
  }

  static Class<?> getClass(String className, String packageName) {
    var classPath = format("%s.%s", packageName, className.substring(0, className.lastIndexOf('.')));
    try {
      return Class.forName(classPath);
    } catch (ClassNotFoundException e) {
      String message = format("Unable to create a class instance of [%s]", classPath);
      throw new IllegalStateException(message, e);
    }
  }

  @SuppressWarnings("squid:S5960")
  static void testRecordClass(Class<?> classz) {
    Constructor<?>[] constructors = classz.getConstructors();
    LOGGER.trace("Class {} has {} constructors.", classz, constructors.length);
    if (constructors.length > 1) {
      LOGGER.warn("Record class {} has multiple constructors which is not supported.", classz);
    }
    Constructor<?> constructor = constructors[0];
    LOGGER.trace(constructor);
    List<Object> constructorArguments = new ArrayList<>();
    Parameter[] parameters = constructor.getParameters();
    Map<Method, Object> expected = new HashMap<>();
    for (Parameter parameter : parameters) {
      Type type = parameter.getParameterizedType();
      Class<?> classType = parameter.getType();
      Object value = getValue(classType, type);
      String name = parameter.getName();
      Method accessorMethod;
      try {
        accessorMethod = classz.getMethod(name);
      } catch (NoSuchMethodException | SecurityException e) {
        // bugger - for a record there should be a public get method
        throw new IllegalStateException(e);
      }
      expected.put(accessorMethod, value);
      LOGGER.trace("Parameter [{}] is of type [{}] classType [{}] with name [{}] and value [{}]",
        parameter, type, classType, name, value);
      constructorArguments.add(value);
    }
    Object objectUnderTest;
    try {
      objectUnderTest = constructor.newInstance(constructorArguments.toArray());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
    LOGGER.debug(objectUnderTest);
    for (Entry<Method, Object> entry : expected.entrySet()) {
      Method method = entry.getKey();
      try {
        assertEquals(entry.getValue(), method.invoke(objectUnderTest));
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        LOGGER.warn(e.getMessage(), e);
        fail(e.getMessage());
      }
    }
  }

  static Object getValue(Class<?> classz, Type type) {
    if (Collection.class.isAssignableFrom(classz)) {
      if (type instanceof ParameterizedType parameterizedType) {
        LOGGER.info(parameterizedType.getRawType());
        LOGGER.info(parameterizedType.getActualTypeArguments()[0]);
      } else {
        LOGGER.warn("Class {} is of type Collection that is not supported. :(", type);
      }
    }
    Supplier<Object> supplier = BeanTestHelper.SUPPLIER_CONSTRUCTORS.get(classz);
    if (supplier != null) {
      return supplier.get();
    }
    return null;
  }

  private static <T> Map<Class<T>, Function<T, Object>> getFunctionConstructors() {
    return null;
  }

  private static Map<Class<?>, Supplier<Object>> getSupplierConstructors() {
    Map<Class<?>, Supplier<Object>> constructors = new HashMap<>();
    constructors.put(Boolean.class, BeanTestHelper.SECURE_RANDOM::nextBoolean);
    constructors.put(Double.class, BeanTestHelper.SECURE_RANDOM::nextDouble);
    constructors.put(Float.class, BeanTestHelper.SECURE_RANDOM::nextFloat);
    constructors.put(Integer.class, BeanTestHelper.SECURE_RANDOM::nextInt);
    constructors.put(Long.class, BeanTestHelper.SECURE_RANDOM::nextLong);
    constructors.put(String.class, BeanTestHelper::getRandomString);
    return constructors;
  }

  private static String getRandomString() {
    int leftLimit = 20; // ' ' the space character
    int rightLimit = 126; // ~ tilde
    int size = BeanTestHelper.SECURE_RANDOM.nextInt(20);
    return BeanTestHelper.SECURE_RANDOM.ints(size, leftLimit, rightLimit)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  private static SecureRandom getSecureRandom() {
    try {
      return SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

}
