package js.hera.auto.engine;

import com.jslib.automata.DeviceAction;

public class Switch extends DeviceAction
{
	boolean switchState;

	protected void update()
	{
		invoke("binary-light", "setState", switchState);
	}
}