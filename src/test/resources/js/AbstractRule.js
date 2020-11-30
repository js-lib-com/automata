class AbstractRule {
	constructor() {
		this.parameters = [];
	}
	
	condition() {
		return true;
	}
	
	update() {
		if(typeof this.action === 'undefined') {
			throw 'Missing action.';
		}
		if(this.condition()) {
			this.action.execute();
		}
		else {
			this.action.reset();
		}
	}
};
