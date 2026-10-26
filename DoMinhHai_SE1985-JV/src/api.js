const BASE = "http://localhost:8080/api/v1";

export const tokenStore = {
   get: () => localStorage.getItem("token"),
   set: (t) => localStorage.setItem("token", t),
   clear: () => localStorage.removeItem("token"),
};

export async function api(path, { method = "GET", body } = {}) {
   const headers = {};
   const token = tokenStore.get();
   if (token) headers.Authorization = `Bearer ${token}`;
   if (body !== undefined) headers["Content-Type"] = "application/json";

   const res = await fetch(BASE + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
   });

   const text = await res.text();
   let data;
   try {
      data = text ? JSON.parse(text) : null;
   } catch {
      data = null; // phản hồi không phải JSON
   }

   if (!res.ok) {
      if (res.status === 401 && token && path !== "/auth/login") {
         tokenStore.clear();
         window.dispatchEvent(new Event("auth-expired"));
      }
      throw new Error(data?.detail || data?.message || `Lỗi ${res.status}`);
   }
   return data;
}
