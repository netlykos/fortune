package org.netlykos;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeanTestHelper {

  private static final SecureRandom SECURE_RANDOM = getSecureRandom();
  private static final Map<Class<?>, Supplier<Object>> SUPPLIER_CONSTRUCTORS = getSupplierConstructors();
  private static final Integer CALLER_DEPTH = 3;

  static final Pattern EXCLUDE_TEST_CLASSES = Pattern.compile("^((?!.*Test.class).)*$");

  private BeanTestHelper() {
    /* do nothing constructor */
  }

  private static final Logger LOGGER = LogManager.getLogger(BeanTestHelper.class);

  public static List<Bean> testPackageExcludeTests() {
    return testPackage(getCaller(CALLER_DEPTH).getPackageName(), EXCLUDE_TEST_CLASSES);
  }

  public static List<Bean> testPackage() {
    return testPackage(getCaller(CALLER_DEPTH).getPackageName());
  }

  public static List<Bean> testPackage(String packageName) {
    return testPackage(packageName, null);
  }

  public static List<Bean> testPackage(Pattern excludePattern) {
    return testPackage(getCaller(CALLER_DEPTH).getPackageName(), excludePattern);
  }

  public static List<Bean> testPackage(String packageName, Pattern excludePattern) {
    Set<Class<?>> classesInPackage = findAllClassesUsingClassLoader(packageName, excludePattern);
    List<Bean> beans = new ArrayList<>();
    LOGGER.trace("Identified {} beans in package {} for testing.", classesInPackage.size(), packageName);
    for (Class<?> classz : classesInPackage) {
      Bean bean = testBean(classz);
      if (bean == null) {
        LOGGER.warn("Could not create an instance of {}", classz);
        continue;
      }
      beans.add(bean);
    }
    return Collections.unmodifiableList(beans);
  }

  public static Bean testBean(Class<?> classz) {
    Bean bean = createBean(classz);
    if (bean == null) {
      LOGGER.warn("Could not create an instance of {}", classz);
      return null;
    }
    LOGGER.trace("Testing bean: {}", bean.classz());
    bean.testBeanAccessors();
    bean.testToString();
    bean.testHashCode();
    return bean;
  }

  static Set<Class<?>> findAllClassesUsingClassLoader(String packageName, Pattern excludePattern) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packageName.replaceAll("[.]", "/");
    try {
      Enumeration<URL> resources = classLoader.getResources(path);
      List<File> dirs = new ArrayList<>();
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        dirs.add(new File(resource.getFile()));
      }
      Set<Class<?>> classes = new HashSet<>();
      for (File directory : dirs) {
        classes.addAll(findClasses(directory, packageName, excludePattern));
      }
      return classes;
    } catch (IOException ioe) {
      String message = format("Unable to find resources to iterate over for package name [%s]", packageName);
      throw new IllegalStateException(message, ioe);
    }
  }

  private static Set<Class<?>> findClasses(File directory, String packageName, Pattern excludePattern) {
    if (!directory.exists()) {
      return Collections.emptySet();
    }
    return Stream.of(directory.listFiles())
        .filter(file -> file.getName().endsWith(".class"))
        .filter(file -> excludePattern == null || excludePattern.matcher(file.getName()).matches())
        .map(file -> getClass(file.getName(), packageName))
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

  static Bean createBean(Class<?> classz) {
    if (classz.isRecord()) {
      LOGGER.trace("Class {} is a record.", classz);
      return createRecord(classz);
    }
    return null;
  }

  static Bean createRecord(Class<?> classz) {
    Constructor<?>[] constructors = classz.getConstructors();
    LOGGER.trace("Class {} has {} constructors.", classz, constructors.length);
    if (constructors.length != 1) {
      LOGGER.warn("Record class {} has multiple constructors which is not supported.", classz);
      return null;
    }
    Constructor<?> constructor = constructors[0];
    List<BeanProperty> beanProperties = new ArrayList<>();
    Parameter[] parameters = constructor.getParameters();
    List<Object> constructorArguments = new ArrayList<>();
    for (Parameter parameter : parameters) {
      Type type = parameter.getParameterizedType();
      Class<?> classType = parameter.getType();
      Object value = getValue(classType, type);
      String name = parameter.getName();
      Method getMethod = getMethodWithName(classz, name);
      beanProperties.add(new BeanProperty(name, classType, getMethod, null, value));
      constructorArguments.add(value);
    }
    Object self = createObjectWithArguments(constructor, constructorArguments);
    LOGGER.debug(self);
    return new Bean(classz, self, beanProperties);
  }

  static Method getMethodWithName(Class<?> classz, String name, Class<?>... parameterizedType) {
    try {
      return classz.getMethod(name, parameterizedType);
    } catch (NoSuchMethodException | SecurityException e) {
      // bugger - for a record there should be a public get method
      throw new IllegalStateException(e);
    }
  }

  static Object createObjectWithArguments(Constructor<?> constructor, List<Object> constructorArguments) {
    try {
      return constructor.newInstance(constructorArguments.toArray());
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  static Object getValue(Class<?> classz, Type type) {
    if (Collection.class.isAssignableFrom(classz)) {
      if (type instanceof ParameterizedType parameterizedType) {
        Type collectionType = parameterizedType.getRawType();
        Type collectionGenericType = parameterizedType.getActualTypeArguments()[0];
        if (List.class.isAssignableFrom((Class<?>)collectionType)) {
          return BeanTestHelper.getList((Class<?>)collectionGenericType);
        }
        if (Set.class.isAssignableFrom((Class<?>)collectionType)) {
          return BeanTestHelper.getSet((Class<?>)collectionGenericType);
        }
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

  private static Map<Class<?>, Supplier<Object>> getSupplierConstructors() {
    Map<Class<?>, Supplier<Object>> constructors = new HashMap<>();
    constructors.put(Boolean.class, BeanTestHelper.SECURE_RANDOM::nextBoolean);
    constructors.put(Double.class, BeanTestHelper.SECURE_RANDOM::nextDouble);
    constructors.put(Float.class, BeanTestHelper.SECURE_RANDOM::nextFloat);
    constructors.put(Integer.class, BeanTestHelper.SECURE_RANDOM::nextInt);
    constructors.put(Long.class, BeanTestHelper.SECURE_RANDOM::nextLong);
    constructors.put(String.class, BeanTestHelper::getRandomString);

    constructors.put(Timestamp.class, BeanTestHelper::getRandomTimestamp);
    constructors.put(java.sql.Date.class, BeanTestHelper::getRandomSqlDate);
    constructors.put(java.util.Date.class, BeanTestHelper::getRandomUtilDate);
    constructors.put(LocalDate.class, BeanTestHelper::getRandomLocalDate);
    constructors.put(LocalTime.class, BeanTestHelper::getRandomLocalTime);
    constructors.put(LocalDateTime.class, BeanTestHelper::getRandomLocalDateTime);
    return constructors;
  }

  private static String getRandomString() {
    // https://www.w3schools.com/charsets/ref_html_utf8.asp
    int leftLimit = 32;
    int rightLimit = 1320;
    int size = BeanTestHelper.SECURE_RANDOM.nextInt(50);

    return BeanTestHelper.SECURE_RANDOM.ints(size, leftLimit, rightLimit)
        .filter(Character::isDefined)
        .mapToObj(Character::toString)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

  private static Timestamp getRandomTimestamp() {
    long timeInMillis = BeanTestHelper.SECURE_RANDOM.nextLong(0, Long.MAX_VALUE);
    return new Timestamp(timeInMillis);
  }

  private static java.util.Date getRandomUtilDate() {
    long timeInMillis = BeanTestHelper.SECURE_RANDOM.nextLong(0, Long.MAX_VALUE);
    return new java.util.Date(timeInMillis);
  }

  private static java.sql.Date getRandomSqlDate() {
    long timeInMillis = BeanTestHelper.SECURE_RANDOM.nextLong(0, Long.MAX_VALUE);
    return new java.sql.Date(timeInMillis);
  }

  private static LocalDate getRandomLocalDate() {
    int year = BeanTestHelper.SECURE_RANDOM.nextInt(Year.MIN_VALUE, Year.MAX_VALUE);
    Month month = Month.of(BeanTestHelper.SECURE_RANDOM.nextInt(1, 12));
    int day = BeanTestHelper.SECURE_RANDOM.nextInt(1, 31);
    return LocalDate.of(year, month, day);
  }

  private static LocalTime getRandomLocalTime() {
    int hour = BeanTestHelper.SECURE_RANDOM.nextInt(0, 23);
    int minute = BeanTestHelper.SECURE_RANDOM.nextInt(0, 59);
    int second = BeanTestHelper.SECURE_RANDOM.nextInt(0, 59);
    int nanoOfSecond = BeanTestHelper.SECURE_RANDOM.nextInt(0, 999999999);
    return LocalTime.of(hour, minute, second, nanoOfSecond);
  }

  private static LocalDateTime getRandomLocalDateTime() {
    return LocalDateTime.of(getRandomLocalDate(), getRandomLocalTime());
  }

  private static <T> List<T> getList(Class<T> t) {
    int size = BeanTestHelper.SECURE_RANDOM.nextInt(10);
    return buildCollection(t, size, new ArrayList<>(size));
  }

  private static <T> Set<T> getSet(Class<T> t) {
    int size = BeanTestHelper.SECURE_RANDOM.nextInt(10);
    return buildCollection(t, size, new HashSet<>(size));
  }

  private static <C extends Collection<T>, T> C buildCollection(Class<T> t, int size, C collection) {
    Supplier<Object> supplier = BeanTestHelper.SUPPLIER_CONSTRUCTORS.get(t);
    if (supplier != null) {
      for (int i = 0; i < size; i++) {
        @SuppressWarnings("unchecked")
        T object = (T) supplier.get();
        collection.add(object);
      }
    }
    return collection;
  }

  private static Class<?> getCaller(int depth) {
    try {
      return Class.forName(Thread.currentThread().getStackTrace()[depth].getClassName());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  private static SecureRandom getSecureRandom() {
    try {
      return SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

}
