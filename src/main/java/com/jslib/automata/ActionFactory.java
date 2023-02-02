package com.jslib.automata;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.jslib.lang.BugError;

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
      return clazz.getDeclaredConstructor().newInstance();
    }
    catch(InstantiationException e) {
      throw new BugError("Action |%s| is not instantiable.", actionClassName);
    }
    catch(IllegalAccessException e) {
      throw new BugError("Action |%s| constructor is not accessible.", actionClassName);
    }
    catch(IllegalArgumentException e) {
      throw new BugError("Impossible condition: illegal argument on action |%s| default constructor.", actionClassName);
    }
    catch(InvocationTargetException e) {
      Throwable t = e.getCause() != null ? e.getCause() : e;
      throw new BugError("Error execution action |%s| constructor. Root cause: %s: %s", actionClassName, t.getClass().getCanonicalName(), t.getMessage());
    }
    catch(NoSuchMethodException e) {
      throw new BugError("Action |%s| has no default constructor.", actionClassName);
    }
    catch(SecurityException e) {
      throw new BugError("Action |%s| default constructor is not public.", actionClassName);
    }
  }

  public void removeClassLoader(String actionClassName)
  {
    synchronized(LOADERS) {
      LOADERS.remove(actionClassName);
    }
  }
}
