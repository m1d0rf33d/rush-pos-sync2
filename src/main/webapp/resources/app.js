import React, {Component} from "react";
import HomeComponent from "./home-component.js";

class App extends Component {

	constructor(props) {
		super(props);
	}
	
    render() {
        return (<HomeComponent/>);
    }
}

export default App;
