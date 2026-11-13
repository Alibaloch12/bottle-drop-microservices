import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Warehouse from './pages/Warehouse';
import UsersDirectory from './pages/UsersDirectory';
import AddUser from './pages/AddUser';
import Bars from './pages/Bars';
import DropPoints from './pages/DropPoints';

function ProtectedRoute({ children }) {
  const user = localStorage.getItem('user');
  const location = useLocation();
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
}

function RoleProtectedRoute({ children, allowedRoles }) {
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    return <Navigate to="/login" replace />;
  }
  
  const user = JSON.parse(userStr);
  
  if (!allowedRoles.includes(user.role)) {
    // If the user's role is not allowed, redirect them to the dashboard
    return <Navigate to="/" replace />;
  }
  
  return children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          {/* Dashboard is accessible to everyone */}
          <Route index element={<Dashboard />} />
          
          {/* Users and Add User: Only Planners */}
          <Route path="users" element={
            <RoleProtectedRoute allowedRoles={['PLANNER']}>
              <UsersDirectory />
            </RoleProtectedRoute>
          } />
          <Route path="add-user" element={
            <RoleProtectedRoute allowedRoles={['PLANNER']}>
              <AddUser />
            </RoleProtectedRoute>
          } />
          
          {/* Warehouse: Planners, Warehouse Assistants, Stockists */}
          <Route path="warehouse" element={
            <RoleProtectedRoute allowedRoles={['PLANNER', 'WAREHOUSE_ASSISTANT', 'STOCKIST']}>
              <Warehouse />
            </RoleProtectedRoute>
          } />
          
          {/* Bars: Planners, Bartenders, Visitors */}
          <Route path="bars" element={
            <RoleProtectedRoute allowedRoles={['PLANNER', 'BARTENDER', 'VISITOR']}>
              <Bars />
            </RoleProtectedRoute>
          } />
          
          {/* Drop Points: Planners, Stockists, Visitors */}
          <Route path="drop-points" element={
            <RoleProtectedRoute allowedRoles={['PLANNER', 'STOCKIST', 'VISITOR']}>
              <DropPoints />
            </RoleProtectedRoute>
          } />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
