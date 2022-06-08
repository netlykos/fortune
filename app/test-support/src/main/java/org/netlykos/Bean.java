package org.netlykos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public record Bean(Object self, List<BeanProperty> beanProperties) {

  public Bean(Object self, List<BeanProperty> beanProperties) {
    this.self = self;
    this.beanProperties = Collections.unmodifiableList(beanProperties);
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
    Class<?> c = self.getClass();
    Method method = BeanTestHelper.getMethodWithName(c, "toString");
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
    Class<?> c = self.getClass();
    Method method = BeanTestHelper.getMethodWithName(c, "hashCode");
    if (method == null) {
      return;
    }
    try {
      assertEquals(method.invoke(this), method.invoke(this));
      Object other = BeanTestHelper.createBean(c);
      assertNotEquals(method.invoke(this), method.invoke(other));
    } catch (ReflectiveOperationException e) {
      fail(e.getMessage());
    }
  }

}
