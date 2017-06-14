import React, {Component} from "react";
import {connect} from "react-redux";
import NavComponent from "./component/nav-component";
import SidebarComponent from "./component/sidebar-component";
import {HashRouter as Router, Route, Switch, hashHistory} from "react-router-dom";
import HelloComponent  from './component/hello-component'
import MerchantComponent from './component/merchant-component'
import BranchComponent from './component/branch-component'
import AccountComponent from './component/account-component'

class HomeComponent extends Component {

	
	render() {
		return (
			<div>

				<Router history="{hashHistory}">
					<div>
						<NavComponent/>
						<div className="row">
							<div className="col-xs-2">
								<SidebarComponent/>
							</div>
							<div className="col-xs-8">

								<div className="content-container">
									<Switch>
										<Route exact path="/" component={HelloComponent} />
										<Route path="/merchant" component={MerchantComponent}/>
										<Route path="/branch" component={BranchComponent}/>
										<Route path="/account" component={AccountComponent}/>
									</Switch>
								</div>

							</div>
						</div>
					</div>

				</Router>
			</div>
		);
	}
	
}

function mapStateToProps(state) {
	
 return {
     activeUser: state.activeUser == null ? false : state.activeUser
 };
}

export default connect(mapStateToProps)(HomeComponent);