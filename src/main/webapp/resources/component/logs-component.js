import React, {Component} from 'react'
import axios from 'axios'


class LogsComponent extends Component {

    constructor() {
        super();
        this.state = {
            errorLogs: [],
            accessLogs: []
        }
    }

    componentDidMount() {
        this.fetchLogs();
        this.fetchErrorLogs();
    }

    fetchLogs() {
        let tref = this;
        axios.get('/rush-pos-sync/logs?type=access', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                accessLogs: resp.data
            })
        }).catch(function(error) {
            alert(error);
        });
    }

    fetchErrorLogs() {
        let tref = this;
        axios.get('/rush-pos-sync/logs?type=error', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                errorLogs: resp.data
            })
        }).catch(function(error) {
            alert(error);
        });
    }


    render() {
        return (
          <div>
              <div className="row">
                  <label className="prim-label">ACCESS LOGS</label>
              </div>

              <div className="row logs-div">
                  {
                      this.state.accessLogs.map(function(log) {
                        return <p>{log}</p>;
                      })
                  }

              </div>
              <br/>

              <div className="row">
                  <label className="prim-label">ERROR LOGS</label>
              </div>

              <div className="row error-logs-div">
                  {
                      this.state.errorLogs.map(function(log) {
                          return <p>{log}</p>;
                      })
                  }

              </div>

          </div>
        );
    }

}


export default LogsComponent;