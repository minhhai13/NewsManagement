import "./App.css";
import { Routes, Route } from "react-router";

import Layout from "./components/Layout";
import { RequireAuth, RequireRole, HomeRedirect } from "./auth/Guards";
import Login from "./pages/Login";
import PublicNews from "./pages/PublicNews";
import MyArticles from "./pages/MyArticles";
import EditorQueue from "./pages/EditorQueue";
import EditorArticles from "./pages/EditorArticles";
import AdminOverride from "./pages/AdminOverride";
import CategoryManage from "./pages/CategoryManage";
import UserManage from "./pages/UserManage";
import NotFound from "./pages/NotFound";

function App() {
   return (
      <Routes>
         <Route path="/login" element={<Login />} />
         <Route path="/public" element={<PublicNews />} />

         <Route element={<RequireAuth />}>
            <Route element={<Layout />}>
               <Route index element={<HomeRedirect />} />

               <Route element={<RequireRole roles={["CONTRIBUTOR"]} />}>
                  <Route path="my-articles" element={<MyArticles />} />
               </Route>

               <Route element={<RequireRole roles={["EDITOR"]} />}>
                  <Route path="queue" element={<EditorQueue />} />
               </Route>

               <Route element={<RequireRole roles={["EDITOR", "ADMIN"]} />}>
                  <Route path="published" element={<EditorArticles />} />
               </Route>

               <Route element={<RequireRole roles={["ADMIN"]} />}>
                  <Route path="override" element={<AdminOverride />} />
                  <Route path="category" element={<CategoryManage />} />
                  <Route path="users" element={<UserManage />} />
               </Route>

               <Route path="*" element={<NotFound />} />
            </Route>
         </Route>
      </Routes>
   );
}

export default App;
