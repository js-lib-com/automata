package com.jslib.automata;

import java.util.ArrayList;
import java.util.List;

import com.jslib.util.Strings;

public class ActionClass
{
  private String packageName;
  private List<String> importClasses;
  private String className;
  private List<ActionField> fields;
  private List<String> codeLines;

  public ActionClass()
  {
    this.importClasses = new ArrayList<>();
    this.fields = new ArrayList<>();
    this.codeLines = new ArrayList<>();
  }

  public void setPackageName(String packageName)
  {
    this.packageName = packageName;
  }

  public String getPackageName()
  {
    return packageName;
  }

  public void addImportClass(String importClass)
  {
    importClasses.add(importClass);
  }

  public List<String> getImportClasses()
  {
    return importClasses;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getClassName()
  {
    return className;
  }

  public void addField(ActionField field)
  {
    fields.add(field);
  }

  public List<ActionField> getFields()
  {
    return fields;
  }

  public void setDeviceInterface(String deviceInterface)
  {
    importClasses.add(deviceInterface);
    String simpleName = Strings.last(deviceInterface, '.');
    fields.add(new ActionField(simpleName, simpleName.toLowerCase()));
  }

  public void addCodeLine(String codeLine)
  {
    codeLines.add(codeLine);
  }

  public List<String> getCodeLines()
  {
    return codeLines;
  }
}
