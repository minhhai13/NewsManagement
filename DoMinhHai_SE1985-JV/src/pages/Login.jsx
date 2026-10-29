import { useState } from "react";
import { Form, Button, Card, Alert } from "react-bootstrap";
import { Link, useNavigate } from "react-router";
import { useAuth, HOME_BY_ROLE } from "../auth/AuthContext";

export default function Login() {
   const [username, setUsername] = useState("");
   const [password, setPassword] = useState("");
   const [error, setError] = useState("");
   const [busy, setBusy] = useState(false);
   const { login } = useAuth();
   const navigate = useNavigate();

   const handleLogin = async (e) => {
      e.preventDefault();
      setError("");
      setBusy(true);
      try {
         const user = await login(username, password);
         navigate(HOME_BY_ROLE[user.role], { replace: true });
      } catch (err) {
         setError(err.message);
      } finally {
         setBusy(false);
      }
   };

   return (
      <div className="d-flex align-items-center justify-content-center vh-100">
         <Card style={{ width: 400 }} className="shadow p-3">
            <Card.Body>
               <h2 className="text-center fw-bold mb-4">FUNews Login</h2>
               {error && <Alert variant="danger">{error}</Alert>}
               <Form onSubmit={handleLogin}>
                  <Form.Group className="mb-3">
                     <Form.Label>Username</Form.Label>
                     <Form.Control value={username} onChange={(e) => setUsername(e.target.value)} autoFocus required />
                  </Form.Group>
                  <Form.Group className="mb-4">
                     <Form.Label>Password</Form.Label>
                     <Form.Control type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                  </Form.Group>
                  <Button type="submit" variant="primary" className="w-100 fw-bold" disabled={busy}>Đăng nhập</Button>
               </Form>
               <div className="text-center mt-3">
                  <Link to="/public">Xem tin đã đăng (không cần đăng nhập)</Link>
               </div>
            </Card.Body>
         </Card>
      </div>
   );
}
