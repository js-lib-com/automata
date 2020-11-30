package js.hera.auto.engine;

import com.jslib.automata.DeviceAction;

public class AirConditioning extends DeviceAction
{
  private double dHTTemperature;
  private double dHTHumidity;

  protected void update()
  {
  	boolean state = dHTTemperature > 22.5 && dHTHumidity > 60;
    invoke("air-conditioning", "setState", state);
  }
}
