import React, {Component} from 'react'
import axios from 'axios'
import ReactDataGrid from 'react-data-grid'
import ReactDOM from 'react-dom'
import Modal from 'react-modal'

const columns = [
    { resizable: true, key: 'name', name: 'Name' },

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
        this.getUserStatus();
    }

    getRoles() {
        let tref = this;
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        axios.get('/rush/role?merchantId='+merchantId, {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {

            tref.setState({
                roles: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    getUserStatus() {
        let tref = this;

        axios.get('/rush/user/status', {
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

        axios.get('/rush/merchant', {
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
        axios.get('/rush/user?merchantId=' + merchantId, {
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


    onRowClick(rowIdx, row) {

        this.getRoles();
        
        this.setState({
            updateModalIsOpen: true,
            user: {
                id: row.id,
                name: row.name,
                roleId: row.roleId
            }
        });

    }

    updateValue() {

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
                    <div className="row">
                        <div className="col-xs-6">
                            Role
                        </div>
                        <div className="col-xs-6">
                            <select ref="role" value={this.state.user.roleId} required>
                                {
                                    this.state.roles.map(function(role) {
                                        return <option key={role.id}
                                                       value={role.id}>{role.name}</option>;
                                    })
                                }
                            </select>
                        </div>
                    </div>

                </Modal>

                <div className="row">
                    <div className="col-xs-2">
                        <label className="h1">Accounts</label>
                    </div>
                    <div className="col-xs-2">
                        <select ref="merchant" defaultValue="" required>
                            {
                                this.state.merchants.map(function(merchant) {
                                    return <option key={merchant.id}
                                                   value={merchant.id}>{merchant.name}</option>;
                                })
                            }
                        </select>
                    </div>
                    <div>

                        <button className="btn btn-default" onClick={this.getUsers.bind(this)}>Search</button>
                    </div>
                </div>
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