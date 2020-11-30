package com.jslib.automata;

public class ActionField
{
  private String type;
  private String name;

  public ActionField(String type, String name)
  {
    this.type = type;
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public String getName()
  {
    return name;
  }

  @Override
  public String toString()
  {
    return "ActionField [type=" + type + ", name=" + name + "]";
  }
}
