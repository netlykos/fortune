package org.netlykos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public record BeanProperty(String name, Type type, Method getMethod, Method setMethod, Object value) {

  @SuppressWarnings("squid:S5960")
  public void assertValueEquals(Object self) {
    try {
      var actual = getMethod.invoke(self);
      assertEquals(value, actual);
    } catch (ReflectiveOperationException e) {
      fail(e.getMessage());
    }
  }

}
