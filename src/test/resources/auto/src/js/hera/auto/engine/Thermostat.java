package js.hera.auto.engine;

import com.jslib.automata.DeviceAction;

public class Thermostat extends DeviceAction
{
  private double thermostatPresetTemperature;
  private double temperatureSensorValue;

  protected void update()
  {
    if(thermostatPresetTemperature > temperatureSensorValue) {
      invoke("thermostat", "setON");
    }
    else {
      invoke("thermostat", "setOFF");
    }
  }
}
