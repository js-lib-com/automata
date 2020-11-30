package com.jslib.automata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.util.Params;
import js.util.Strings;

public class Rule
{
  private static final Log log = LogFactory.getLog(Rule.class);

  private String name;
  private String display;
  private String icon;

  private List<EventDescriptor> events;

  private transient Action action;
  private String actionClassName;
  private Map<String, String> actionParameters;

  public Rule()
  {

  }

  /**
   * Construct a rule instance usable for set operations. It has only name initialized, which name is acting as primary
   * key, being used to uniquely identifying a rule in the set.
   * 
   * @param name unique rule name.
   * @see #hashCode()
   * @see #equals(Object)
   */
  public Rule(String name)
  {
    this.name = name;
  }

  public Rule(Rule source, Action action)
  {
    this.name = source.getName();
    this.display = source.getDisplay();
    this.icon = source.getIcon();
    this.events = source.getEvents();

    this.action = action;
    this.actionClassName = source.getActionClassName();
    this.actionParameters = source.getActionParameters();
  }

  public Rule(String name, List<EventDescriptor> events, String actionClassName)
  {
    this.name = name;
    this.events = events;
    this.actionClassName = actionClassName;
  }

  public Rule(String name, List<EventDescriptor> events, Action action, boolean persistent)
  {
    this.name = name;
    this.events = events;
    this.action = action;
    this.actionClassName = action.getClass().getName();
    this.actionParameters = persistent ? new HashMap<>() : null;
  }

  /**
   * Hook executed after rule loaded from external persistence storage, on engine startup. If rules is persistent action
   * instance fields are initialized with stored values then action executed.
   * 
   * @param action rule action.
   */
  public void postLoad(Action action)
  {
    Params.notNull(action, "Action instance");
    this.action = action;
    if(actionParameters != null) {
      this.action.setParameters(actionParameters);
      this.action.execute();
    }
  }

  public void preSave()
  {
    if(actionParameters != null) {
      action.getParameters(actionParameters);
    }
  }

  public void update(Map<String, String> event)
  {
    String eventName = event.get("deviceName");

    EventDescriptor eventDescriptor = getEventDescriptor(eventName);
    for(String eventParameter : eventDescriptor.getParameters()) {
      if(eventParameter == null) {
        continue;
      }
      String value = event.get(eventParameter);
      if(value == null) {
        log.warn("Invalid event |%s|. Missing parameter |%s|. Ignore action |%s|.", eventName, eventParameter, action.getName());
        return;
      }

      String actionParameter = actionParameter(eventName, eventParameter);
      if(!action.setParameter(actionParameter, value)) {
        throw new BugError("Invalid source code for action |%s|. Missing parameter |%s|.", action.getName(), actionParameter);
      }
    }

    action.execute();
  }

  public String getName()
  {
    return name;
  }

  public String getDisplay()
  {
    return display;
  }

  public String getIcon()
  {
    return icon;
  }

  public List<EventDescriptor> getEvents()
  {
    return events;
  }

  public String getActionClassName()
  {
    return actionClassName;
  }

  public Map<String, String> getActionParameters()
  {
    return actionParameters;
  }

  public void setAction(Action action)
  {
    Params.notNull(action, "Action instance");
    this.action = action;
  }

  public Action getAction()
  {
    return action;
  }

  public void setEvents(List<EventDescriptor> events)
  {
    this.events = events;
  }

  public EventDescriptor getEventDescriptor(String eventName)
  {
    if(events == null) {
      return null;
    }
    for(EventDescriptor eventDescriptor : events) {
      if(eventDescriptor.getDeviceName().equals(eventName)) {
        return eventDescriptor;
      }
    }
    return null;
  }

  public boolean hasEvent(String eventName)
  {
    return getEventDescriptor(eventName) != null;
  }

  private static String actionParameter(String eventName, String eventParameter)
  {
    if(eventName.contains("-")) {
      eventName = Strings.toMemberName(eventName);
    }
    eventName = eventName.replaceAll(" ", "").replaceAll("<br>", "");
    StringBuilder builder = new StringBuilder();
    builder.append(Character.toLowerCase(eventName.charAt(0)));
    builder.append(eventName.substring(1));
    builder.append(Character.toUpperCase(eventParameter.charAt(0)));
    builder.append(eventParameter.substring(1));
    return builder.toString();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    Rule other = (Rule)obj;
    if(name == null) {
      if(other.name != null) return false;
    }
    else if(!name.equals(other.name)) return false;
    return true;
  }
}
