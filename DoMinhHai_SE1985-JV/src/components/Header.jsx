import { Navbar, Container, Button } from "react-bootstrap";
import { Link, useNavigate } from "react-router";
import logo from "../images/NMS.png";
import { useAuth } from "../auth/AuthContext";
import { ROLE_LABEL } from "../constants";

export default function Header() {
   const { user, logout } = useAuth();
   const navigate = useNavigate();

   const handleLogout = () => {
      logout();
      navigate("/login");
   };

   return (
      <Navbar bg="white" className="border-bottom shadow-sm py-2">
         <Container fluid className="px-4">
            <Navbar.Brand as={Link} to="/" className="d-flex align-items-center gap-2">
               <img src={logo} alt="FUNews" width="40" height="40" className="rounded-circle object-fit-cover" />
               <span className="fw-bold fs-5">FUNews</span>
            </Navbar.Brand>
            <div className="d-flex align-items-center gap-3">
               <Link to="/public" className="text-decoration-none">Trang đọc tin</Link>
               <span>
                  {user.name} <span className="badge text-bg-secondary">{ROLE_LABEL[user.role]}</span>
               </span>
               <Button variant="outline-dark" size="sm" onClick={handleLogout}>Đăng xuất</Button>
            </div>
         </Container>
      </Navbar>
   );
}
