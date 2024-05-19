package org.netlykos.fortune.service;

import java.util.Collection;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;
import org.netlykos.fortune.exception.FortuneNotFoundException;

public interface FortuneManagerService {

  public Fortune getFortune(String category, int cookie);
  public Fortune getRandomFortune();
  public Fortune getRandomFortuneFromCategory(String category);
  public FortuneCategory getFortuneCategory(String category);
  public Collection<FortuneCategory> getFortuneCategories();

  public default Fortune getFortune(String category, Integer cookie)
    throws FortuneNotFoundException
  {
    if (category != null) {
      if (cookie != null) {
        return getFortune(category, (int)cookie);
      }
      // we got a category, but no cookie number - select a random cookie from the category
      return getRandomFortuneFromCategory(category);
    }
    return getRandomFortune();
  }

}
