package org.netlykos.fortune.service;

import java.util.Collection;

import org.netlykos.fortune.beans.Fortune;
import org.netlykos.fortune.beans.FortuneCategory;

public interface FortuneManagerService {

  public Fortune getFortune(String category, int cookie);
  public Fortune getRandomFortune();
  public Fortune getRandomFortuneFromCategory(String category);
  public FortuneCategory getFortuneCategory(String category);
  public Collection<FortuneCategory> getFortuneCategories();

}
