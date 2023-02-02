package com.jslib.automata;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.jslib.lang.ManagedLifeCycle;

public interface Automata extends ManagedLifeCycle
{
  void setDeviceActionHandler(DeviceActionHandler handler);

  void handleEvent(Map<String, String> event);

  ActionDescriptor createActionClass(String actionDisplay) throws IOException;

  void saveAction(ActionDescriptor actionDescriptor) throws IOException, ClassNotFoundException;

  Set<ActionDescriptor> getActions();

  void removeAction(String actionClassName);

  Set<Rule> getRules();

  void saveRule(Rule rule) throws ClassNotFoundException, IOException;

  void removeRule(String ruleName) throws IOException;
}
