package com.jslib.automata;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.automata.util.Files;
import com.jslib.net.client.HttpRmiTransaction;
import com.jslib.util.Strings;

public abstract class DeviceAction extends Action
{
  protected static final Log log = LogFactory.getLog(DeviceAction.class);

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

  protected void notify(String message, Object... args)
  {
    if(args.length > 0) {
      message = String.format(message, args);
    }
    log.debug("Send notification |%s|.", message);
    RMI("http://Android.local:8080/sync", "com.jslib.hera.agent.Controller", "hello", new Object[]
    {
        message
    }, null);
  }

  @SuppressWarnings("unchecked")
  protected <T> T RMI(String implementationURL, String className, String methodName, Object[] arguments, Class<T> returnType)
  {
    HttpRmiTransaction rmi = HttpRmiTransaction.getInstance(implementationURL);
    rmi.setConnectionTimeout(4000);
    rmi.setReadTimeout(8000);
    rmi.setHeader("X-Requested-With", "automata");

    rmi.setMethod(className, methodName);
    rmi.setArguments(arguments);

    try {
      if(returnType != null) {
        rmi.setReturnType(returnType);
        return (T)rmi.exec(null);
      }

      rmi.exec(null);
    }
    catch(Throwable t) {
      log.dump("Error on device RMI:", t);
    }
    return null;
  }

  protected void record(String measurement, double value)
  {
    try {
      URL url = new URL("http://localhost:8086/write?db=sensors");
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      String post = String.format("%s value=%f", measurement, value);
      log.debug("Post |%s| to |%s|.", post, url);
      Files.copy(new ByteArrayInputStream(post.getBytes()), connection.getOutputStream());

      int responseCode = connection.getResponseCode();
      if(responseCode < 200 || responseCode >= 300) {
        log.warn("Fail to write on InfluxDB. Response code |%d|.", responseCode);
      }
    }
    catch(Throwable t) {
      log.dump("Error on device record:", t);
    }
  }
}
