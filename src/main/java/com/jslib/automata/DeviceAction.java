package com.jslib.automata;

import js.log.Log;
import js.log.LogFactory;
import js.net.client.HttpRmiTransaction;
import js.util.Strings;

public abstract class DeviceAction extends Action
{
  private static final Log log = LogFactory.getLog(DeviceAction.class);

  private DeviceActionHandler handler;

  public void setHandler(DeviceActionHandler handler)
  {
    this.handler = handler;
  }

  protected void invoke(String deviceName, String methodName, Object... arguments)
  {
    log.debug("%s:%s:%s", deviceName, methodName, Strings.join(arguments));
    try {
      handler.invokeDeviceAction(deviceName, methodName, arguments);
    }
    catch(Throwable t) {
      log.dump("Error on device action:", t);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T RMI(String implementationURL, String className, String methodName, Object[] arguments, Class<T> returnType) throws Exception
  {
    HttpRmiTransaction rmi = HttpRmiTransaction.getInstance(implementationURL);
    rmi.setConnectionTimeout(4000);
    rmi.setReadTimeout(8000);
    rmi.setHeader("X-Requested-With", "automata");

    rmi.setMethod(className, methodName);
    rmi.setArguments(arguments);
    if(returnType != null) {
      rmi.setReturnType(returnType);
      return (T)rmi.exec(null);
    }

    rmi.exec(null);
    return null;
  }
}
