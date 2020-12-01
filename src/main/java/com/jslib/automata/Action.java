package com.jslib.automata;

import java.lang.reflect.Field;
import java.util.Map;

import js.converter.Converter;
import js.converter.ConverterRegistry;
import js.lang.BugError;
import js.util.Classes;
import js.util.Params;

public abstract class Action
{
  protected abstract void update();

  private final String name;
  protected transient ErrorHandler errorHandler;

  protected Action()
  {
    this.name = getClass().getSimpleName();
    this.errorHandler = new ErrorHandler();
  }

  public void execute()
  {
    try {
      update();
    }
    catch(Throwable throwable) {
      errorHandler.execute(throwable);
    }
  }

  public String getName()
  {
    return name;
  }

  public boolean setParameter(String name, String value)
  {
    Field field = Classes.getOptionalField(getClass(), name);
    if(field == null) {
      return false;
    }
    Classes.setFieldValue(this, field, value);
    return true;
  }

  public void setParameters(Map<String, String> parameters)
  {
    for(Map.Entry<String, String> parameter : parameters.entrySet()) {
      setParameter(parameter.getKey(), parameter.getValue());
    }
  }

  public void getParameters(Map<String, String> parameters)
  {
    Params.notNull(parameters, "Parameters map");
    Converter converter = ConverterRegistry.getConverter();
    try {
      for(Field field : getClass().getDeclaredFields()) {
        field.setAccessible(true);
        parameters.put(field.getName(), converter.asString(field.get(this)));
      }
    }
    catch(IllegalAccessException e) {
      throw new BugError("Illegal access on field with accessibility set to true.");
    }
  }

  public Action replicateState(Action sourceAction)
  {
    try {
      for(Field field : getClass().getDeclaredFields()) {
        field.setAccessible(true);

        Field sourceField = null;
        try {
          sourceField = sourceAction.getClass().getDeclaredField(field.getName());
        }
        catch(NoSuchFieldException unused) {
          continue;
        }

        assert sourceField != null;
        sourceField.setAccessible(true);
        field.set(this, sourceField.get(sourceAction));
      }
    }
    catch(IllegalAccessException e) {
      throw new BugError(e);
    }
    catch(SecurityException e) {
      throw new BugError("Security exception on field with accessibility set to true.");
    }
    return this;
  }
}
