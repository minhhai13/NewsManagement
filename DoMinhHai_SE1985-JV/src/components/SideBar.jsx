import { useEffect, useState } from "react";
import { Nav, Badge } from "react-bootstrap";
import { NavLink } from "react-router";
import { api } from "../api";
import { useAuth } from "../auth/AuthContext";

const MENU = {
   CONTRIBUTOR: [{ to: "/my-articles", label: "Bài của tôi" }],
   EDITOR: [
      { to: "/queue", label: "Hàng chờ duyệt" },
      { to: "/published", label: "Bài đã duyệt / đã đăng" },
   ],
   ADMIN: [
      { to: "/override", label: "Bài viết (override)" },
      { to: "/published", label: "Bài đã duyệt / đã đăng" },
      { to: "/users", label: "Người dùng" },
      { to: "/category", label: "Danh mục" },
   ],
};

export default function Sidebar() {
   const { user } = useAuth();
   const [changes, setChanges] = useState(0);

   // Thông báo: số bài bị yêu cầu sửa, làm mới mỗi 20 giây hoặc khi trang "Bài của tôi" báo thay đổi.
   useEffect(() => {
      if (user.role !== "CONTRIBUTOR") return;
      const load = () => api("/news/my/changes-count").then((r) => setChanges(r.count)).catch(() => {});
      load();
      const timer = setInterval(load, 20000);
      window.addEventListener("articles-changed", load);
      return () => {
         clearInterval(timer);
         window.removeEventListener("articles-changed", load);
      };
   }, [user.role]);

   return (
      <div className="p-3 bg-white border-end" style={{ width: 250, overflowY: "auto" }}>
         <div className="text-uppercase text-secondary fw-bold mb-3 mt-2 px-2" style={{ fontSize: 12, letterSpacing: 1 }}>
            Menu
         </div>
         <Nav className="flex-column gap-1">
            {MENU[user.role].map((item) => (
               <Nav.Link key={item.to} as={NavLink} to={item.to} className="nav-link-side p-2 d-flex justify-content-between">
                  {item.label}
                  {item.to === "/my-articles" && changes > 0 && (
                     <Badge bg="danger" pill title="Bài cần sửa">{changes}</Badge>
                  )}
               </Nav.Link>
            ))}
         </Nav>
      </div>
   );
}
