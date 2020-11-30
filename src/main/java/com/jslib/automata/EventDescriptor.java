package com.jslib.automata;

import java.util.Arrays;
import java.util.List;

public class EventDescriptor
{
  private String deviceName;
  private List<String> parameters;

  public EventDescriptor()
  {
  }

  public EventDescriptor(List<String> parameters)
  {
    this.parameters = parameters;
  }

  public EventDescriptor(String deviceName, String... parameters)
  {
    this.deviceName = deviceName;
    this.parameters = Arrays.asList(parameters);
  }

  public String getDeviceName()
  {
    return deviceName;
  }

  public List<String> getParameters()
  {
    return parameters;
  }
}
