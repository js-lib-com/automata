package com.jslib.automata;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jslib.automata.util.Files;

import js.json.Json;
import js.lang.BugError;
import js.lang.GType;
import js.log.Log;
import js.log.LogFactory;
import js.util.Classes;
import js.util.Params;
import js.util.Strings;

public class AutomataImpl implements Automata
{
  private static final Log log = LogFactory.getLog(Automata.class);

  private final Json json;
  private final File baseDir;
  private final File actionsFile;
  private final File rulesFile;

  private final ActionFactory actionFactory;
  private final Set<ActionDescriptor> actions;
  private final Object actionsMutex = new Object();

  private final Set<Rule> rules;
  private final Object rulesMutex = new Object();

  private DeviceActionHandler handler;

  public AutomataImpl(File baseDir) throws IOException
  {
    this.json = Classes.loadService(Json.class);
    this.baseDir = baseDir;
    this.actionsFile = new File(baseDir, "actions.json");
    this.rulesFile = new File(baseDir, "rules.json");

    this.actionFactory = new ActionFactory(baseDir);
    if(this.actionsFile.exists()) {
      this.actions = json.parse(new FileReader(actionsFile), new GType(Set.class, ActionDescriptor.class));
    }
    else {
      this.actions = new HashSet<>();
    }

    if(this.rulesFile.exists()) {
      this.rules = json.parse(new FileReader(rulesFile), new GType(Set.class, Rule.class));
    }
    else {
      this.rules = new HashSet<>();
    }
  }

  public AutomataImpl(File baseDir, Set<Rule> rules)
  {
    this.json = Classes.loadService(Json.class);
    this.baseDir = baseDir;
    this.actionFactory = new ActionFactory(baseDir);
    this.actionsFile = new File(baseDir, "actions.json");
    this.rulesFile = new File(baseDir, "rules.json");
    this.actions = new HashSet<>();
    this.rules = rules;
  }

  @Override
  public void postConstruct() throws Exception
  {
    for(Rule rule : this.rules) {
      rule.postLoad(actionFactory.getAction(rule.getActionClassName()));
    }
  }

  @Override
  public void preDestroy() throws IOException
  {
    json.stringify(new FileWriter(actionsFile), actions);

    for(Rule rule : this.rules) {
      rule.preSave();
    }
    json.stringify(new FileWriter(rulesFile), rules);
  }

  @Override
  public void setDeviceActionHandler(DeviceActionHandler handler)
  {
    this.handler = handler;
    for(Rule rule : this.rules) {
      ((DeviceAction)rule.getAction()).setHandler(handler);
    }
  }

  @Override
  public ActionDescriptor createActionClass(String actionDisplay) throws IOException
  {
    Map<String, String> args = new HashMap<>();
    // action display is used to create action class simple name by removing spaces
    args.put("class-simple-name", actionDisplay.replaceAll(" ", ""));
    SourceCode code = new SourceCode(Strings.injectVariables(Classes.getResourceAsString("action-class-template"), args));

    ActionDescriptor action = new ActionDescriptor();
    action.setClassName(code.getClassName());
    action.setDisplay(actionDisplay);
    action.setCode(code.getCode());
    return action;
  }

  @Override
  public void saveAction(ActionDescriptor actionDescriptor) throws IOException, ClassNotFoundException
  {
    SourceCode sourceCode = new SourceCode(actionDescriptor.getCode());
    String actionClassName = sourceCode.getClassName();
    log.debug("Create action class |%s|.", actionClassName);
    Set<Rule> actionRules = removeRulesByAction(actionClassName);
    log.debug("Existing rules removed for action |%s|.", actionClassName);

    sourceCode.save(baseDir);
    sourceCode.compile(baseDir);
    log.debug("Source code compiled for action |%s|.", actionClassName);

    // removing current action class loader ensure next .getAction call creates a new action class loader
    actionFactory.removeClassLoader(actionClassName);
    log.debug("Byte code loaded for action |%s|.", actionClassName);
    for(Rule actionRule : actionRules) {
      Action action = actionFactory.getAction(actionClassName).replicateState(actionRule.getAction());
      ((DeviceAction)action).setHandler(handler);
      synchronized(rulesMutex) {
        rules.add(new Rule(actionRule, action));
      }
    }
    log.debug("New rules created for action |%s|.", actionClassName);

    // on this scope exit, 'actionRules' variable become unreachable and will be garbage collected
    // together with old rules and related action class loaded that, in theory, will unload old action classes

    synchronized(actionsMutex) {
      actions.remove(new ActionDescriptor(actionDescriptor.getClassName()));
      actions.add(actionDescriptor);
    }
    preDestroy();
  }

  @Override
  public void removeAction(String actionClassName)
  {
    Params.notNullOrEmpty(actionClassName, "Action class name");
    if(isActionUsed(actionClassName)) {
      throw new BugError("Attempt to remove used action |%s|.", actionClassName);
    }
    actionFactory.removeClassLoader(actionClassName);
    Files.sourceFile(baseDir, actionClassName).delete();
    Files.classFile(baseDir, actionClassName).delete();

    synchronized(actionsMutex) {
      actions.remove(new ActionDescriptor(actionClassName));
    }
  }

  @Override
  public Set<ActionDescriptor> getActions()
  {
    return actions;
  }

  @Override
  public void saveRule(Rule rule) throws ClassNotFoundException, IOException
  {
    if(rule.getEvents() == null) {
      List<EventDescriptor> events = new ArrayList<>();
      events.add(new EventDescriptor(new ArrayList<String>()));
      rule.setEvents(events);
    }

    Action action = actionFactory.getAction(rule.getActionClassName());
    ((DeviceAction)action).setHandler(handler);
    rule.setAction(action);

    synchronized(rulesMutex) {
      rules.remove(new Rule(rule.getName()));
      rules.add(rule);
    }

    preDestroy();
  }

  @Override
  public void removeRule(String ruleName) throws IOException
  {
    synchronized(rulesMutex) {
      rules.remove(new Rule(ruleName));
    }

    preDestroy();
  }

  @Override
  synchronized public void handleEvent(Map<String, String> event)
  {
    // by convention event name is the source device name
    String eventName = event.get("deviceName");
    if(eventName == null) {
      throw new BugError("Invalid event format. Review connector logic.");
    }

    Set<Rule> rules = getEventRules(eventName);
    assert rules != null;
    if(rules.isEmpty()) {
      log.warn("Not registered event |%s|. Ignore it.", eventName);
      return;
    }

    for(Rule rule : rules) {
      rule.update(event);
    }
  }

  @Override
  public Set<Rule> getRules()
  {
    return rules;
  }

  private boolean isActionUsed(String actionClassName)
  {
    Iterator<Rule> it = rules.iterator();
    synchronized(rulesMutex) {
      while(it.hasNext()) {
        if(actionClassName.equals(it.next().getActionClassName())) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<Rule> removeRulesByAction(String actionClassName)
  {
    Set<Rule> actionRules = new HashSet<>();
    Iterator<Rule> it = rules.iterator();
    synchronized(rulesMutex) {
      while(it.hasNext()) {
        final Rule rule = it.next();
        if(rule.getActionClassName().equals(actionClassName)) {
          actionRules.add(rule);
          it.remove();
        }
      }
    }
    return actionRules;
  }

  private Set<Rule> getEventRules(String eventName)
  {
    Set<Rule> eventRules = new HashSet<>();
    synchronized(rulesMutex) {
      for(Rule rule : rules) {
        if(rule.hasEvent(eventName)) {
          eventRules.add(rule);
        }
      }
    }
    return eventRules;
  }
}
