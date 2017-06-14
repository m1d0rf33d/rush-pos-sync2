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
                screens: row.screens
            }
        })
    }

    postRole() {

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
                    <div className="role-modal">
                        <div className="row">
                            <label>Role Details</label>
                         </div>
                       <div className="row">
                           <div className="col-xs-6">
                               Name
                           </div>
                           <div className="col-xs-6">
                               <input type="text" ref="role" />
                           </div>
                       </div>

                        <div className="row">
                            <div className="col-xs-6">
                                Access
                            </div>
                            <div className="col-xs-6">
                                {this.state.role.screens.map(function(screen) {
                                    return (
                                        <div className="row">
                                            <div className="col-xs-6">
                                                {screen.name}
                                            </div>
                                            <div className="col-xs-6">
                                                <input type="checkbox" checked={screen.checked}/>
                                            </div>
                                         </div>
                                    );
                                })}
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
                        <button className="btn btn-primary prim-btn" onClick={this.postRole.bind(this)}>Add</button>
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