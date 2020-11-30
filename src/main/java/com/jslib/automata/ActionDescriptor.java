package com.jslib.automata;

import java.io.IOException;

public class ActionDescriptor
{
  private String className;
  private String display;
  private String icon;
  private String code;

  public ActionDescriptor()
  {
  }

  public ActionDescriptor(String className)
  {
    this.className = className;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getClassName()
  {
    return className;
  }

  public void setDisplay(String display)
  {
    this.display = display;
  }

  public String getDisplay()
  {
    return display;
  }

  public String getIcon()
  {
    return icon;
  }

  public void setCode(String code) throws IOException
  {
    this.code = code;
  }

  public String getCode()
  {
    return code;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    ActionDescriptor other = (ActionDescriptor)obj;
    if(className == null) {
      if(other.className != null) return false;
    }
    else if(!className.equals(other.className)) return false;
    return true;
  }
}
