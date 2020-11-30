class AbstractAction {
	constructor(deviceName) {
		this.device = DeviceManager.getDevice(deviceName);
	}
	
	execute() {
		
	}
	
	reset() {
		
	}
};
