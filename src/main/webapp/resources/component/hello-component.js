import React, { Component } from 'react'

class HelloComponent extends Component {


    render() {
        return (
          <div>
             <h4> Migrated site from Angular JS to React JS, Apache2 server to Tomcat. Notify developer if problems are encountered. </h4>
              <br/>
              <ul>
                  <li>Merchant - add, update merchant details</li>
                  <li>Branch   - virtual keyboard config can be set per branch</li>
                  <li>Accounts - assign role to each account created on Rush CMS</li>
                  <li>Roles    - add, update role access</li>
                  <li>Logs     - view access logs this will help monitor user activity and result, error logs for unexpected errors encoutered by the app</li>
                  <li>History  - rush pos sync user activity records for monitoring</li>
            </ul>
          </div>
        );
    }
}

export default HelloComponent;5