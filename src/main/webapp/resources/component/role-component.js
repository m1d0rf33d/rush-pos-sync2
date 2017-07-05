import React, {Component} from 'react'
import ReactDataGrid from 'react-data-grid'
import axios from 'axios'
import ReactDOM from 'react-dom'
import Modal from 'react-modal'


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



const columns = [
    { resizable: true, key: 'name', name: 'ROLE' }
]


class RoleComponent extends Component {

    constructor() {
        super();
        this.state = {
            roles: [],
            merchants: [],
            role: {
                'screens': []
            }
        }
    }

    componentDidMount() {
        this.getMerchants();
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

    updateValue() {

        this.state.role.name = ReactDOM.findDOMNode(this.refs.role).value;
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

    onRowClick(rowIdx, row) {
        console.log(row);
        this.setState({
            updateModalIsOpen: true,
            role: {
                id: row.roleId,
                name: row.name,
                screens: row.screens
            }
        })
    }

    postRole() {
        let register           = ReactDOM.findDOMNode(this.refs.REGISTER).checked;
        let member_profile     = ReactDOM.findDOMNode(this.refs.MEMBER_PROFILE).checked;
        let give_points        = ReactDOM.findDOMNode(this.refs.GIVE_POINTS).checked;
        let guest_purchase     = ReactDOM.findDOMNode(this.refs.GUEST_PURCHASE).checked;
        let give_points_ocr    = ReactDOM.findDOMNode(this.refs.GIVE_POINTS_OCR).checked;
        let give_stamps        = ReactDOM.findDOMNode(this.refs.GIVE_STAMPS).checked;
        let pay_with_points    = ReactDOM.findDOMNode(this.refs.PAY_WITH_POINTS).checked;
        let redeem_rewards    = ReactDOM.findDOMNode(this.refs.REDEEM_REWARDS).checked;
        let issue_rewards    = ReactDOM.findDOMNode(this.refs.ISSUE_REWARDS).checked;
        let transaction_view    = ReactDOM.findDOMNode(this.refs.TRANSACTIONS_VIEW).checked;
        let ocr_settings    = ReactDOM.findDOMNode(this.refs.OCR_SETTINGS).checked;
        let offline_transactions    = ReactDOM.findDOMNode(this.refs.OFFLINE_TRANSACTIONS).checked;


        let merchant_id = ReactDOM.findDOMNode(this.refs.merchant).value;
        if (merchant_id == '-1') {
            this.setState({
                updateModalIsOpen: false,
                alertIsOpen: true,
                message: 'Invalid merchant'
            })
            return;
        }
        let data = {
            'name': this.state.role.name,
            'merchantId': merchant_id,
            'roleId': this.state.role.id,
            'screens': [
                {
                   'name': 'REGISTER',
                    'checked': register
                },
                {
                    'name': 'MEMBER_PROFILE',
                    'checked': member_profile
                },
                {
                    'name': 'GIVE_POINTS',
                    'checked': give_points
                },
                {
                    'name': 'GUEST_PURCHASE',
                    'checked': guest_purchase
                },
                {
                    'name': 'GIVE_POINTS_OCR',
                    'checked': give_points_ocr
                },
                {
                    'name': 'GIVE_STAMPS',
                    'checked': give_stamps
                },
                {
                    'name': 'PAY_WITH_POINTS',
                    'checked': pay_with_points
                },
                {
                    'name': 'REDEEM_REWARDS',
                    'checked': redeem_rewards
                },
                {
                    'name': 'ISSUE_REWARDS',
                    'checked': issue_rewards
                },
                {
                    'name': 'TRANSACTIONS_VIEW',
                    'checked': transaction_view
                },
                {
                    'name': 'OCR_SETTINGS',
                    'checked': ocr_settings
                },
                {
                    'name': 'OFFLINE_TRANSACTIONS',
                    'checked': offline_transactions
                }
            ]
        }

        let postConfig = {
            method: 'POST',
            url: '/rush-pos-sync/role',
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

                tref.getRoles();

            }).catch(function(error) {
            alert(error);
        })
    }

    closeUpdateModal() {
        this.setState({
            updateModalIsOpen: false
        })
    }

    addRole() {

        let tref = this;
        axios.get('/rush-pos-sync/screens', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {

            tref.setState({
                updateModalIsOpen: true,
                role: {
                    screens: resp.data
                }
            });
        }).catch(function(error) {
            alert(error);
        });

    }


    closeAlert() {
        this.setState({
            alertIsOpen: false
        })
    }


    render() {
        return (
            <div>
                <Modal
                    isOpen={this.state.alertIsOpen}
                    onAfterOpen={this.afterOpenAlert}
                    onRequestClose={this.closeAlert.bind(this)}
                    style=  {customStyles}
                    contentLabel="Example Modal"
                > {this.state.message}
                </Modal>
                <Modal
                    isOpen={this.state.updateModalIsOpen}
                    onRequestClose={this.closeUpdateModal.bind(this)}
                    style=  {customStyles}
                    contentLabel="Example Modal"
                >
                    <div className="role-modal">
                        <div className="row">
                            <label className="prim-label">Role Details</label>
                         </div>
                         <hr/>
                       <div className="row">
                           <div className="col-xs-3">
                               Name
                           </div>
                           <div className="col-xs-9">
                               <input onChange={this.updateValue.bind(this)} type="text" ref="role" value={this.state.role.name}/>
                           </div>
                       </div>
                        <br/>
                        <div className="row">
                            <div className="col-xs-3">
                                Access
                            </div>
                            <div className="col-xs-9">
                                {this.state.role.screens.map((screen,idx) => {

                                    return (
                                        <div className="row">
                                            <div className="col-xs-6">
                                                {screen.name}
                                            </div>
                                            <div className="col-xs-6">
                                                <input ref={screen.name} className={screen.name} type="checkbox" defaultChecked={screen.checked}/>
                                            </div>
                                         </div>
                                    );
                                })}
                            </div>
                        </div>
                        <br/>
                        <div className="row">
                            <div className="col-xs-3"></div>
                            <div className="col-xs-3">
                                <button className="btn btn-primary prim-btn" onClick={this.postRole.bind(this)}>Submit</button>
                            </div>
                            <div className="col-xs-3">
                                <button className="btn btn-default prim-btn" onClick={this.closeUpdateModal.bind(this)}>Cancel</button>
                            </div>
                        </div>
                    </div>
                    
                </Modal>
                <div className="row">
                    <label className="prim-label">ROLE SETTINGS</label>
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
                        <button className="btn btn-primary prim-btn" onClick={this.getRoles.bind(this)}>Search</button>
                    </div>
                    <div className="col-xs-2">
                        <button className="btn btn-primary prim-btn" onClick={this.addRole.bind(this)}>Add</button>
                    </div>

                </div>
                <br/>
                <div className="row">
                    <ReactDataGrid
                        columns={columns}
                        rowGetter={rowNumber =>  this.state.roles[rowNumber] }
                        rowsCount={this.state.roles.length}
                        minHeight={300}
                        minWidth={900}
                        onRowClick={this.onRowClick.bind(this)}
                    />
                </div>
            </div>
        );

    }


}

export default RoleComponent;