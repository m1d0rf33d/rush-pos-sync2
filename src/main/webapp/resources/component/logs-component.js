import React, {Component} from 'react'
import axios from 'axios'


class LogsComponent extends Component {

    constructor() {
        super();
        this.state = {
            logs: []
        }
    }

    componentDidMount() {
        this.fetchLogs();
    }

    fetchLogs() {
        let tref = this;
        axios.get('/rush-pos-sync/logs', {
            headers: {
                'Content-type': 'application/json'
            }
        }).then(function(resp) {
            tref.setState({
                logs: resp.data
            });
        }).catch(function(error) {
            alert(error);
        });
    }


    render() {
        return (
          <div>
              <div className="row">
                  <label className="prim-label">LOGS</label>
              </div>
              <hr/>
              <div className="row logs-div">
                  {
                      this.state.logs.map(function(log) {
                        return <p>{log}</p>;
                      })
                  }

              </div>

          </div>
        );
    }

}


export default LogsComponent;