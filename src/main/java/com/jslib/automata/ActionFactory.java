package com.jslib.automata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import js.lang.BugError;

public class ActionFactory
{
  private final File baseDir;
  private final Map<String, ActionClassLoader> LOADERS = new HashMap<>();

  public ActionFactory(File baseDir)
  {
    this.baseDir = baseDir;
  }

  public Action getAction(String actionClassName) throws ClassNotFoundException
  {
    ActionClassLoader loader = null;
    synchronized(LOADERS) {
      loader = LOADERS.get(actionClassName);
      if(loader == null) {
        loader = new ActionClassLoader(baseDir);
        LOADERS.put(actionClassName, loader);
      }
    }
    assert loader != null;

    Class<? extends Action> clazz = loader.loadClass(actionClassName);
    try {
      return clazz.newInstance();
    }
    catch(InstantiationException e) {
      throw new BugError("Action class |%s| is not instantiable.", actionClassName);
    }
    catch(IllegalAccessException e) {
      throw new BugError("Action class |%s| constructor is not accessible.", actionClassName);
    }
  }

  public void removeClassLoader(String actionClassName)
  {
    synchronized(LOADERS) {
      LOADERS.remove(actionClassName);
    }
  }
}
