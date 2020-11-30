class ActuatorAction extends AbstractAction {
	constructor(deviceName) {
		super(deviceName);
	}
	
	execute() {
		this.device.set(true);
	}
	
	reset() {
		this.device.set(false);
	}
};