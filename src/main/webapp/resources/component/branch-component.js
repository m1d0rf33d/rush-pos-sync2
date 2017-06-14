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

    getBranches() {
        let tref = this;
        let merchantId = ReactDOM.findDOMNode(this.refs.merchant).value;
        axios.get('/rush/branch?merchant=' + merchantId, {
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
                name: row.name,
                withVk: row.withVk,
                id: row.id
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
                name: branch,
                withVk: withVk,
                id: this.state.branch.id,
                merchantId: tref.state.merchant.id
            }
        })
    }

    updateBranch() {
        let data =  {
            'name': this.state.branch.name,
            'withVk': this.state.branch.withVk == 'on' ? true : false,
            'id': this.state.branch.id,
            'merchantId': this.state.branch.merchantId
        }


        let postConfig = {
            method: 'POST',
            url: '/rush/branch',
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
                            <label > UPDATE BRANCH </label>
                        </div>
                        <br/>
                        <div className="row">
                            <div className="col-xs-6">
                                <label>Name:</label>
                            </div>
                            <div className="col-xs-6">
                                <input onChange={this.updateValue.bind(this)} value={this.state.branch.name} ref="name" id="name-input"  type="text"/>
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
                        </div><br/>



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
                <div className="col-xs-3"><label className="h1">Search Branch:</label></div>
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
                <div className="col-xs-2">
                    <button className="btn btn-primary merchant-add-btn" onClick={this.getBranches.bind(this)}>Search</button>
                </div>
                <div className="col-xs-2">
                    <button className="btn btn-primary merchant-add-btn" onClick={this.addBranch.bind(this)}>Add</button>
                </div>
            </div>
            <div>
                <ReactDataGrid
                    columns={[{ resizable: true,key: 'name', name: 'Branch Name' },
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