import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {updateActiveButton} from "../redux/action/actions.js";

class SidebarComponent extends Component {

    render() {
        return (
            <div className="nav-side-menu sidebar-container">

                <div className="menu-list">

                    <ul classID="menu-content" className="menu-content collapse out">
                        <li className={this.props.activeButton == 'merchant-li' ? 'active' : 'inactive'} onClick={() => this.props.updateActiveButton('merchant-li')}>
                            <Link to="/merchant">
                               Merchant
                            </Link>
                        </li>

                        <li className={this.props.activeButton == 'branch-li' ? 'active' : 'inactive'} onClick={() => this.props.updateActiveButton('branch-li')}>
                            <Link to="/branch">
                                Branch
                            </Link>
                        </li>

                        <li className={this.props.activeButton == 'account-li' ? 'active' : 'inactive'} onClick={() => this.props.updateActiveButton('account-li')}>
                            <Link to="/account">
                                Accounts
                            </Link>
                        </li>
                        <li >
                            <Link to="/role">
                                Roles
                            </Link>
                        </li>


                        <li >
                            <Link to="/merchant">
                                Screen Restrictions
                            </Link>
                        </li>



                    </ul>
                </div>
            </div>
        );
    }
}


function mapStateToProps(state) {
    return {
        activeButton: state.activeButton
    };
}

function matchDispatchToProps(dispatch){
    return bindActionCreators({updateActiveButton: updateActiveButton}, dispatch);
}

export default connect(mapStateToProps,matchDispatchToProps)(SidebarComponent);