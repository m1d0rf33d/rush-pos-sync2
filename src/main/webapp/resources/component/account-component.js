import React, {Component} from 'react'
import axios from 'axios'
import ReactDataGrid from 'react-data-grid'
import ReactDOM from 'react-dom'
import Modal from 'react-modal'

const columns = [
    { resizable: true, key: 'name', name: 'Name' },
    { resizable: true, key: 'role', name: 'Role' }
]


const customStyles = {
    content : {
        top                   : '50%',
        left                  : '50%',
        right                 : 'auto',
        bottom                : 'auto',
        marginRight           : '-50%',
        transform             : 'translate(-50%, -50%)',
        borderRadius          : '0px',
        background            : '#fff',
        border                : '1px solid black'

    }
};


class AccountComponent extends Component {

    constructor() {
        super();
        this.state = {
            users: [],
            merchants: [],
            user: {},
            roles: [],
            updateModalIsOpen: false
        }


    }

    componentDidMount() {
        this.getMerchants();
      //  this.getUserStatus();
    }

    getRoles() {
        let tref = this;
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        axios.get('/rush-pos-sync/role?merchant='+merchantId, {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            console.log(resp.data);
            tref.setState({
                roles: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    getUserStatus() {
        let tref = this;

        axios.get('/rush-pos-sync/user/status', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {

            tref.setState({
                statuses: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    getMerchants() {
        let tref = this;

        axios.get('/rush-pos-sync/merchant', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {

            tref.setState({
                merchants: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    getUsers() {
        let tref = this;
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        axios.get('/rush-pos-sync/account?merchant=' + merchantId, {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {


            tref.setState({
                users: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    postAccount() {
        let data =  {
            'uuid': this.state.user.uuid,
            'roleId': this.state.user.roleId
        }


        let postConfig = {
            method: 'POST',
            url: '/rush-pos-sync/account',
            data: JSON.stringify(data),
            headers: {
                'Content-Type': 'application/json'
            },
            json: true
        };

        let tref = this;

        axios(postConfig)
            .then(function (response) {
                tref.setState({
                    message: 'Branch updated',
                    updateModalIsOpen: false
                });

                tref.getUsers();

            }).catch(function(error) {
            alert(error);
        })
    }

    onRowClick(rowIdx, row) {

        this.getRoles();

        this.setState({
            updateModalIsOpen: true,
            user: {
                uuid: row.uuid,
                role: row.role,
                roleId: row.roleId,
                name: row.name
            }
        });

    }

    updateValue() {
        let roleId = ReactDOM.findDOMNode(this.refs.role).value;
        this.setState({
            user: {
                uuid: this.state.user.uuid,
                roleId: roleId
            }
        })
    }

    closeUpdateModal() {
        this.setState({
            updateModalIsOpen: false
        })
    }

    render() {
        return (
            <div>

                <Modal
                    isOpen={this.state.updateModalIsOpen}
                    onRequestClose={this.closeUpdateModal.bind(this)}
                    style=  {customStyles}
                    contentLabel="Example Modal"
                >
                    <div className="account-modal">
                        <div className="row">
                            <label className="prim-label">Account Details</label>
                        </div>
                        <hr/>
                        <div className="row">
                            <div className="col-xs-6">
                                Name
                            </div>
                            <div className="col-xs-6">
                                <input type="text" disabled value={this.state.user.name} />
                            </div>
                        </div>
                        <br/>
                        <div className="row">
                            <div className="col-xs-6">
                                Role
                            </div>
                            <div className="col-xs-6">
                                <select onChange={this.updateValue.bind(this)} ref="role" value={this.state.user.roleId} required>
                                    <option value="-1">--select--</option>
                                    {
                                        this.state.roles.map(function(role) {
                                            return <option key={role.roleId}
                                                           value={role.roleId}>{role.name}</option>;
                                        })
                                    }
                                </select>
                            </div>
                        </div>
                        <hr/>
                        <div className="row">
                            <div className="col-xs-2">
                            </div>
                            <div className="col-xs-3">
                                <button onClick={this.postAccount.bind(this)} className="btn btn-primary prim-btn">Submit</button>
                            </div>
                            <div className="col-xs-1"></div>
                            <div className="col-xs-3">
                                <button className="btn btn-default prim-btn">Cancel</button>
                            </div>
                            <div className="col-xs-2">
                            </div>
                        </div>
                    </div>

                </Modal>
                <div className="row">
                    <label className="prim-label">ACCOUNTS SETTINGS</label>
                </div>
                <hr/>
                <div className="row">

                    <div className="col-xs-3">
                        <select className="prim-select" ref="merchant" defaultValue="" required>
                            <option value="-1">--select--</option>
                            {
                                this.state.merchants.map(function(merchant) {
                                    return <option key={merchant.id}
                                                   value={merchant.id}>{merchant.name}</option>;
                                })
                            }
                        </select>
                    </div>
                    <div className="col-xs-2">

                        <button className="btn btn-primary prim-btn" onClick={this.getUsers.bind(this)}>Search</button>
                    </div>
                </div>
                <br/>
                <div className="row">
                    <ReactDataGrid
                        columns={columns}
                        rowGetter={rowNumber =>  this.state.users[rowNumber] }
                        rowsCount={this.state.users.length}
                        minHeight={300}
                        minWidth={700}
                        onRowClick={this.onRowClick.bind(this)}
                    />
                </div>

            </div>
        );
    }

}

export default AccountComponent;