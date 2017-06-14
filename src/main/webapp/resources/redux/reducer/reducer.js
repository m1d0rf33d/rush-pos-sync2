import {combineReducers} from 'redux';
import UserReducer from './user-reducer.js';
import UserActiveReducer from './reducer-active-user.js';

const ParentReducer  = combineReducers({
    users: UserReducer,
    loginState: UserActiveReducer,
    activeButton: UserActiveReducer,
    merchants: UserActiveReducer 
});

export default ParentReducer
