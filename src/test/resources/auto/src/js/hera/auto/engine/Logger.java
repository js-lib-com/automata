package js.hera.auto.engine;

import com.jslib.automata.DeviceAction;

public class Logger extends DeviceAction
{
  private String message = "Sensor temperature %02.2f Celsius degrees.";
  private double temperatureSensorValue;

  protected void update()
  {
    invoke("logger", String.format(message, temperatureSensorValue));
  }
}
