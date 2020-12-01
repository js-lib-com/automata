package com.jslib.automata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import js.log.Log;
import js.log.LogFactory;
import js.net.client.HttpRmiTransaction;
import js.util.Files;
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

  protected void notify(String message, Object... args) throws Exception
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

  protected void record(String measurement, double value) throws IOException
  {
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
}
