import React, {Component} from "react";
import axios from "axios";
import Modal from "react-modal";
import ReactDOM from "react-dom";
import ReactDataGrid from "react-data-grid";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";

//react modal style
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


class MerchantComponent extends Component {


    constructor() {
        super();

        this.state = {
            modalIsOpen: false,
            message: '',
            merchants: [],
            alertIsOpen: false,
            statuses: [],
            merchant: {},
            classifications: [],
            merchantTypes: []
        };

        this.openModal      = this.openModal.bind(this);
        this.afterOpenModal = this.afterOpenModal.bind(this);
        this.closeModal     = this.closeModal.bind(this);
    }

    openAlert() {
        this.setState({alertIsOpen: true});
    }

    afterOpenAlert() {

    }

    closeAlert() {
        this.setState({alertIsOpen: false});
    }

    openModal() {
        this.setState({
            modalIsOpen: true,
            merchant: {}
        })
    }

    afterOpenModal() {
        // references are now sync'd and can be accessed.
     //   this.subtitle.style.color = 'black';
    }

    closeModal() {

        this.setState({modalIsOpen: false,
                        merchant: {}});

    }



    updateState() {
        this.setState({
            merchant: {
                name: ReactDOM.findDOMNode(this.refs.name).value,
                merchantApiKey: ReactDOM.findDOMNode(this.refs.merchantKey).value,
                merchantApiSecret: ReactDOM.findDOMNode(this.refs.merchantSecret).value,
                customerApiKey: ReactDOM.findDOMNode(this.refs.customerKey).value,
                customerApiSecret: ReactDOM.findDOMNode(this.refs.customerSecret).value,
                id: this.state.merchant.id,
                status: ReactDOM.findDOMNode(this.refs.status).value,
                type: ReactDOM.findDOMNode(this.refs.type).value,
                classification: ReactDOM.findDOMNode(this.refs.classification).value,
                withVk: ReactDOM.findDOMNode(this.refs.withVk).value
            }
        })
    }

    componentDidMount() {

        this.fetchMerchants();
        this.fetchStatus();
        this.fetchClassifications();
        this.fetchTypes();

    }

    postMerchant() {

        let data =  {
                'id': this.state.merchant.id,
                'name': this.state.merchant.name,
                'merchantApiKey': this.state.merchant.merchantApiKey,
                'merchantApiSecret': this.state.merchant.merchantApiSecret,
                'customerApiKey': this.state.merchant.customerApiKey,
                'customerApiSecret': this.state.merchant.customerApiSecret,
                'status': this.state.merchant.status,
                'merchantType': this.state.merchant.type,
                'classification': this.state.merchant.classification,
                'withVk': this.state.merchant.withVk
        }

        console.log(data);
        if (data.merchantType == undefined || data.classification == undefined
            || data.withVk == undefined || data.merchantType == -1 || data.classification == -1
            || data.withVk == -1 || data.status == -1 || data.status == undefined) {
            this.setState({
                message: 'Invalid input',
                modalIsOpen: false,
                alertIsOpen: true

            })
            return;
        }

        let postConfig = {
            method: 'POST',
            url: '/rush-pos-sync/merchant',
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
                    message: 'Success!',
                    modalIsOpen: false,
                    alertIsOpen: true
                });

                tref.fetchMerchants();

            }).catch(function(error) {
                alert(error);
            })
    }

    fetchMerchants() {
        let tref = this;

        axios.get('/rush-pos-sync/merchant', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                'merchants': resp.data
            });

        }).catch(function(error) {
            alert(error);
        });
    }

    fetchStatus() {
        let tref = this;

        axios.get('/rush-pos-sync/merchant/status', {
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

    fetchTypes() {
        let tref = this;

        axios.get('/rush-pos-sync/merchant/types', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                merchantTypes: resp.data
            });

        }).catch(function(error) {
            alert(error);
        });
    }


    fetchClassifications() {
        let tref = this;

        axios.get('/rush-pos-sync/merchant/classifications', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                classifications: resp.data
            });

        }).catch(function(error) {
            alert(error);
        });
    }


    onRowClick(rowIdx, row) {
        console.log(row);
        this.setState({

            merchant: {
                id: row.id,
                name: row.name,
                merchantApiKey: row.merchantApiKey,
                merchantApiSecret: row.merchantApiSecret,
                customerApiKey: row.customerApiKey,
                customerApiSecret: row.customerApiSecret,
                status: row.status,
                type: row.merchantType,
                classification: row.classification,
                withVk: row.withVk
            },
            modalIsOpen: true
        });
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
                    isOpen={this.state.modalIsOpen}
                    onAfterOpen={this.afterOpenModal}
                    onRequestClose={this.closeModal.bind(this)}
                    style=  {customStyles}
                    contentLabel="Example Modal"
                >
                    <div className="merchant-modal">
                        <div className="row">
                            <label className="prim-label"> Merchant Details </label>
                        </div>
                        <hr/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Name:</label>
                            </div>
                            <div className="col-xs-6">
                                <input className="prim-input" className="prim-input" ref="name" onChange={this.updateState.bind(this)} id="name-input"  type="text" value={this.state.merchant.name}/>
                            </div>
                        </div><br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Merchant API Key:</label>
                            </div>
                            <div className="col-xs-6">
                                <input className="prim-input" ref="merchantKey" onChange={this.updateState.bind(this)} id="merchant-key-input" type="text" value={this.state.merchant.merchantApiKey}/>
                            </div>
                        </div><br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Merchant Api Secret:</label>
                            </div>
                            <div className="col-xs-6">
                                <input className="prim-input" ref="merchantSecret" onChange={this.updateState.bind(this)} id="merchant-secret-input" type="text" value={this.state.merchant.merchantApiSecret}/>
                            </div>
                        </div><br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Customer API Key:</label>
                            </div>
                            <div className="col-xs-6">
                                <input className="prim-input" ref="customerKey" onChange={this.updateState.bind(this)} id="customer-key-input" type="text" value={this.state.merchant.customerApiKey}/>
                            </div>
                        </div><br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Customer API Secret:</label>
                            </div>
                            <div className="col-xs-6">
                                <input className="prim-input" ref="customerSecret" onChange={this.updateState.bind(this)} id="customer-secret-input" type="text" value={this.state.merchant.customerApiSecret}/>
                            </div>
                        </div><br/>

                        <div className="row">
                            <div className="col-xs-6">
                                <label>Status</label>
                            </div>
                            <div className="col-xs-6">
                                <select className="prim-select" onChange={this.updateState.bind(this)} ref="status" value={this.state.merchant.status} >
                                    <option value="-1">--select--</option>
                                    {
                                        this.state.statuses.map(function(status) {
                                            return <option key={status}
                                                           value={status}>{status}</option>;
                                        })
                                    }
                                </select>
                            </div>
                        </div><br/>


                        <div className="row">
                            <div className="col-xs-6">
                                <label>Type</label>
                            </div>
                            <div className="col-xs-6">
                                <select className="prim-select" onChange={this.updateState.bind(this)} ref="type" value={this.state.merchant.type} >
                                    <option value="-1">--select--</option>
                                    {
                                        this.state.merchantTypes.map(function(type) {
                                            return <option key={type}
                                                           value={type}>{type}</option>;
                                        })
                                    }
                                </select>
                            </div>
                        </div><br/>


                        <div className="row">
                            <div className="col-xs-6">
                                <label>Classification</label>
                            </div>
                            <div className="col-xs-6">
                                <select className="prim-select" onChange={this.updateState.bind(this)} ref="classification" value={this.state.merchant.classification} >
                                    <option value="-1">--select--</option>
                                    {
                                        this.state.classifications.map(function(classification) {
                                            return <option key={classification}
                                                           value={classification}>{classification}</option>;
                                        })
                                    }
                                </select>
                            </div>
                        </div><br/>

                        <div className="row">
                            <div className="col-xs-6">
                                <label>Virtual Keyboard</label>
                            </div>
                            <div className="col-xs-6">
                                <select className="prim-select" onChange={this.updateState.bind(this)} ref="withVk" value={this.state.merchant.withVk} >
                                    <option value="-1">--select--</option>
                                    <option value="true">ON</option>
                                    <option value="false">OFF</option>
                                </select>
                            </div>
                        </div><br/>

                        <hr/>
                        <div className="row">
                            <div className="col-xs-3">
                            </div>
                            <div className="col-xs-3">
                                <button className="btn btn-primary prim-btn" onClick={this.postMerchant.bind(this)}> Submit </button>
                            </div>
                            <div className="col-xs-3">
                                <button className="btn btn-default prim-btn" onClick={this.closeModal}>Close</button>
                            </div>
                            <div className="col-xs-3">
                            </div>
                        </div>
                    </div>

                </Modal>
                <div className="row">
                    <label className="prim-label">MERCHANT SETTINGS</label>
                 </div>
                <hr/>
                <div className="row">
                    <div className="col-xs-2">
                        <button className="btn btn-primary merchant-add-btn prim-btn" onClick={this.openModal.bind(this)}>Add</button>
                    </div>
                </div>
                <br/>

                <ReactDataGrid
                    columns={[{ resizable: true,key: 'name', name: 'Name' },
                     { resizable: true, key: 'merchantType', name: 'Type' },
                     { resizable: true, key: 'classification', name: 'Classification' },
                     { resizable: true, key: 'status', name: 'Status' }]}
                    rowGetter={rowNumber =>  this.state.merchants[rowNumber] }
                    rowsCount={this.state.merchants.length}
                    minHeight={400}
                    minWidth={900}
                    onRowClick={this.onRowClick.bind(this)}
                />
            </div>
        )
    }


}

export default MerchantComponent;