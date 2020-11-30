package com.jslib.automata;

import java.util.List;

public class ActionSource
{
  private final ActionClass actionClass;
  private final StringBuilder builder;

  public ActionSource(ActionClass actionClass)
  {
    this.actionClass = actionClass;
    this.builder = new StringBuilder();
  }

  public void generate()
  {
    add("package %s;", actionClass.getPackageName());

    List<String> importClasses = actionClass.getImportClasses();
    if(!importClasses.isEmpty()) {
      add();
      for(String importClass : importClasses) {
        add("import %s;", importClass);
      }
    }

    add();
    add("public class %s extends DeviceAction", actionClass.getClassName());
    add("{");
    for(ActionField field : actionClass.getFields()) {
      add("\tprivate %s %s;", field.getType(), field.getName());
    }
    add();
    add("\tprotected void update()");
    add("\t{");

    for(String codeLine : actionClass.getCodeLines()) {
      add("\t\t%s;", codeLine);
    }

    add("\t}");
    add("}");
  }

  @Override
  public String toString()
  {
    return builder.toString();
  }

  private void add(String format, Object... arguments)
  {
    builder.append(String.format(format, arguments));
    builder.append("\r\n");
  }

  private void add()
  {
    builder.append("\r\n");
  }
}
