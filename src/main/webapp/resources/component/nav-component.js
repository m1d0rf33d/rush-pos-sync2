import React, {Component} from 'react'
import { Redirect } from 'react-router'
import {updateLoginState} from '../redux/action/actions.js'
import {bindActionCreators} from 'redux';
import {connect} from 'react-redux';

class NavComponent extends Component {

    componentDidMount() {

        this.props.updateLoginState(window.localStorage.getItem('auth'));
    }


    render() {
        return (
            <div className="nav-container" >
                <nav className="navbar navbar-default">

                    <div className="container-fluid">
                        <div className="navbar-header">
                            <label className="nav-header-lbl">
                                Rush POS Sync
                            </label>
                        </div>
                        <ul className="nav navbar-nav navbar-right">
                            <li><a  onClick={() =>  this.props.updateLoginState(false)} className="logout-link" href="#">Logout</a></li>
                        </ul>
                    </div>
                </nav>
            </div>

        );
    }

}
//Get apps state and pass it as props to UserList
//> whenever state changes, the UserList will automatically re-render
function mapStateToProps(state) {
    return {
        loginState: state.loginState
    };
}

//Get actions and pass them as props to to UserList
//> now UserList has this.props.selectUser
function matchDispatchToProps(dispatch){
    return bindActionCreators({updateLoginState: updateLoginState}, dispatch);
}

//We don't want to return the plain UserList (component) anymore, we want to return the smart Container
//> UserList is now aware of state and actions
export default connect(mapStateToProps,matchDispatchToProps)(NavComponent);