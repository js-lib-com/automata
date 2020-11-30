package com.jslib.automata;

public interface DeviceActionHandler
{
  Object invokeDeviceAction(String deviceName, String actionName, Object... arguments) throws Exception;
}
