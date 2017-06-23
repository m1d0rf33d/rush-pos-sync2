import React, {Component} from 'react'
import axios from 'axios'
import ReactDataGrid from 'react-data-grid'

const columns = [
    {resizable: true, key: 'user', name: 'Username'},
    {resizable: true, key: 'date', name: 'Date Created'},
    {resizable: true, key: 'activity', name: 'Activity'},
    {resizable: true, key: 'detail', name: 'Details'}
]

class HistoryComponent extends Component {

    constructor() {
        super();
        this.state = {
            userHistories: []
        }
    }

    componentDidMount() {
        this.fetchUserHistories();
    }

    fetchUserHistories() {
        let tref = this;

        axios.get('/rush-pos-sync/user/history', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {

            tref.setState({
                userHistories: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }

    render() {
        return (
          <div>

              <div className="row">
                  <label className="prim-label">USER HISTORY</label>
              </div>
              <hr/>

              <div className="row">
                  <ReactDataGrid
                      columns={columns}
                      rowGetter={rowNumber =>  this.state.userHistories[rowNumber] }
                      rowsCount={this.state.userHistories.length}
                      minHeight={500}
                      minWidth={900}
                  />
              </div>
          </div>
        );
    }

}

export default HistoryComponent;