import { Outlet } from "react-router";
import Header from "./Header";
import Sidebar from "./Sidebar";
import Footer from "./Footer";

export default function Layout() {
   return (
      <div className="d-flex flex-column vh-100 overflow-hidden">
         <Header />
         <div className="d-flex flex-grow-1 overflow-hidden">
            <Sidebar />
            <div className="d-flex flex-column flex-grow-1 overflow-auto">
               <main className="flex-grow-1 p-4">
                  <Outlet />
               </main>
               <Footer />
            </div>
         </div>
      </div>
   );
}
