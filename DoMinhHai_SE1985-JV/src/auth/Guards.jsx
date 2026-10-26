import { Navigate, Outlet } from "react-router";
import { Spinner } from "react-bootstrap";
import { useAuth, HOME_BY_ROLE } from "./AuthContext";

export function RequireAuth() {
   const { user, loading } = useAuth();
   if (loading) return <Spinner className="m-5" />;
   return user ? <Outlet /> : <Navigate to="/login" replace />;
}

export function RequireRole({ roles }) {
   const { user } = useAuth();
   return roles.includes(user.role) ? <Outlet /> : <Navigate to={HOME_BY_ROLE[user.role]} replace />;
}

export function HomeRedirect() {
   const { user } = useAuth();
   return <Navigate to={HOME_BY_ROLE[user.role]} replace />;
}
