package org.netlykos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public record Bean(Class<?> classz, Object self, List<BeanProperty> beanProperties) {

  public static final String TO_STRING = "toString";
  public static final String HASH_CODE = "hashCode";

  public Bean(Class<?> classz, Object self, List<BeanProperty> beanProperties) {
    this.classz = classz;
    this.self = self;
    this.beanProperties = Collections.unmodifiableList(beanProperties);
  }

  public Bean(Object self, List<BeanProperty> beanProperties) {
    this(self.getClass(), self, beanProperties);
  }

  public void testBeanAccessors() {
    // if this is a record then there will be no set method
    if (this.self.getClass().isRecord()) {
      for (BeanProperty beanProperty : beanProperties) {
        beanProperty.assertValueEquals(self);
      }
    }
  }

  @SuppressWarnings("squid:S5960")
  public void testToString() {
    Class<?> c = this.classz;
    Method method = BeanTestHelper.getMethodWithName(c, TO_STRING);
    if (method == null) {
      return;
    }
    try {
      String result = (String) method.invoke(self);
      assertNotNull(result);
      assertTrue(result.length() > 0);
      assertEquals(result, method.invoke(self));
    } catch (ReflectiveOperationException e) {
      fail(e.getMessage());
    }
  }

  public void testHashCode() {
    Class<?> c = this.classz;
    Method method = BeanTestHelper.getMethodWithName(c, HASH_CODE);
    if (method == null) {
      return;
    }
    try {
      assertEquals(method.invoke(this.self), method.invoke(this.self));
      Bean otherBean = BeanTestHelper.createBean(c);
      assertNotEquals(method.invoke(this.self), method.invoke(otherBean.self()));
    } catch (ReflectiveOperationException e) {
      fail(e.getMessage());
    }
  }

}
