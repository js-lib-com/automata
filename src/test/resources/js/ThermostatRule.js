class ThermostatRule extends AbstractRule {
	constructor() {
		super();
		this.parameters = new Parameters();
		this.action = new ActuatorAction("heating-system");
	}
	
	condition() {
		return this.parameters[0].temperature > this.parameters[1].temperature;
	}
};
