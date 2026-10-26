import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { api, tokenStore } from "../api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
   const [user, setUser] = useState(null);
   const [loading, setLoading] = useState(!!tokenStore.get());

   useEffect(() => {
      if (!tokenStore.get()) return;
      api("/auth/me")
         .then(setUser)
         .catch(() => tokenStore.clear())
         .finally(() => setLoading(false));
   }, []);

   useEffect(() => {
      const onExpired = () => setUser(null);
      window.addEventListener("auth-expired", onExpired);
      return () => window.removeEventListener("auth-expired", onExpired);
   }, []);

   const login = useCallback(async (username, password) => {
      const data = await api("/auth/login", { method: "POST", body: { username, password } });
      tokenStore.set(data.token);
      setUser(data.user);
      return data.user;
   }, []);

   const logout = useCallback(() => {
      tokenStore.clear();
      setUser(null);
   }, []);

   return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
   return useContext(AuthContext);
}

// eslint-disable-next-line react-refresh/only-export-components
export const HOME_BY_ROLE = {
   ADMIN: "/override",
   EDITOR: "/queue",
   CONTRIBUTOR: "/my-articles",
};
