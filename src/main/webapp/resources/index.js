import React from 'react'
import ReactDOM from 'react-dom'
import App from './app'
import {createStore, applyMiddleware, compose} from 'redux'
import ParentReducer from './redux/reducer/reducer.js'
import {Provider} from 'react-redux';


const store = createStore(
		ParentReducer
	);

ReactDOM.render(
		<Provider store={store}>
			<App/>
		</Provider>
        ,
  document.getElementById('app')
);
