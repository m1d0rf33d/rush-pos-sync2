import React, {Component} from 'react'
import axios from 'axios'
import ReactDataGrid from 'react-data-grid';
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


class BranchComponent extends Component {

    constructor() {
        super();
        this.state = {
            merchants : [],
            branches : [],
            branch: {}
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

    getBranches() {
        let tref = this;
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        axios.get('/rush-pos-sync/branch?merchant=' + merchantId, {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            let branches = [];
            for (let value of resp.data) {
                value.withVk = value.withVk ? 'on' : 'off';
                branches.push(value);
            }

            tref.setState({
                branches: branches
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    openModal() {
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        this.setState({updateModalIsOpen: true});
        this.setState({
            merchant: {
                id: merchantId
            }
        })
    }

    onRowClick(rowIdx, row) {
        console.log(row);
        this.setState({
            branch: {
                branchName: row.branchName,
                withVk: row.withVk,
                branchId: row.branchId,
                uuid: row.uuid
            }
        })
        this.openModal();
    }

    closeUpdateModal() {
        this.setState({
            updateModalIsOpen: false
        })
    }

    updateValue() {

        let tref = this;

        let branch = ReactDOM.findDOMNode(this.refs.name).value;
        let withVk = ReactDOM.findDOMNode(this.refs.withVk).value;
        this.setState({
            branch: {
                branchName: branch,
                withVk: withVk,
                branchId: this.state.branch.branchId,
                uuid: this.state.branch.uuid
            }
        })
    }

    updateBranch() {
        let data =  {
            'branchName': this.state.branch.branchName,
            'withVk': this.state.branch.withVk == 'on' ? true : false,
            'branchId': this.state.branch.branchId,
            'uuid': this.state.branch.uuid
        }


        let postConfig = {
            method: 'POST',
            url: '/rush-pos-sync/branch',
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

                tref.getBranches();

            }).catch(function(error) {
            alert(error);
        })
    }

    addBranch() {
        this.openModal();
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

                    <div className="branch-modal">
                        <div className="row">
                            <label className="prim-label" > Branch Details </label>
                        </div>
                        <hr/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Name:</label>
                            </div>
                            <div className="col-xs-6">
                                <input disabled onChange={this.updateValue.bind(this)} value={this.state.branch.branchName} ref="name" id="name-input"  type="text"/>
                            </div>
                        </div><br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Virtual Keyboard:</label>
                            </div>
                            <div className="col-xs-6">
                                <select onChange={this.updateValue.bind(this)} ref="withVk" value={this.state.branch.withVk}>
                                    <option value="on">ON</option>
                                    <option value="off">OFF</option>
                                </select>
                            </div>
                        </div><hr/>



                        <div className="row">
                            <div className="col-xs-3">
                            </div>
                            <div className="col-xs-3">
                                <button className="btn btn-primary" onClick={this.updateBranch.bind(this)}> Submit </button>
                            </div>
                            <div className="col-xs-3">
                                <button className="btn btn-default" onClick={this.closeModal}>Close</button>
                            </div>
                            <div className="col-xs-3">
                            </div>
                        </div>
                    </div>
                </Modal>

            <div className="row">
                <label className="prim-label">BRANCH SETTINGS</label>
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
                    <button className="btn btn-primary branch-search prim-btn" onClick={this.getBranches.bind(this)}>Search</button>
                </div>
            </div>
            <br/>
            <div>
                <ReactDataGrid
                    columns={[{ resizable: true,key: 'branchName', name: 'Branch Name' },
                    { resizable: true, key: 'withVk', name: 'Virtual Keyboard' }]}
                    rowGetter={rowNumber =>  this.state.branches[rowNumber] }
                    rowsCount={this.state.branches.length}
                    minHeight={300}
                    minWidth={700}
                    onRowClick={this.onRowClick.bind(this)}
                />
            </div>
            </div>
        )
    }

}

export default BranchComponent;