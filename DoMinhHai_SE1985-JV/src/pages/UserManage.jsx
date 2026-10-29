import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Form, Badge, Alert } from "react-bootstrap";
import { api } from "../api";
import { ROLE_LABEL } from "../constants";
import { useAuth } from "../auth/AuthContext";
import FormModal from "../components/FormModal";
import ConfirmModal from "../components/ConfirmModal";

const EMPTY = { username: "", password: "", name: "", role: "CONTRIBUTOR", status: 1 };

export default function UserManage() {
   const { user: me } = useAuth();
   const [users, setUsers] = useState([]);
   const [search, setSearch] = useState("");
   const [error, setError] = useState("");
   const [info, setInfo] = useState("");
   const [formError, setFormError] = useState("");
   const [showForm, setShowForm] = useState(false);
   const [editing, setEditing] = useState(null);
   const [form, setForm] = useState(EMPTY);
   const [deleting, setDeleting] = useState(null);

   const load = useCallback(async () => {
      try {
         setUsers(await api("/users"));
      } catch (e) {
         setError(e.message);
      }
   }, []);

   useEffect(() => {
      load();
   }, [load]);

   const filtered = users.filter((u) => `${u.username} ${u.name}`.toLowerCase().includes(search.toLowerCase()));

   const openForm = (item = null) => {
      setEditing(item);
      setFormError("");
      setForm(item ? { username: item.username, password: "", name: item.name, role: item.role, status: item.status } : EMPTY);
      setShowForm(true);
   };

   const submit = async () => {
      try {
         const body = { ...form, status: Number(form.status) };
         if (editing) await api(`/users/${editing.id}`, { method: "PUT", body });
         else await api("/users", { method: "POST", body });
         setShowForm(false);
         load();
      } catch (e) {
         setFormError(e.message);
      }
   };

   const confirmDelete = async () => {
      try {
         const res = await api(`/users/${deleting.id}`, { method: "DELETE" });
         setInfo(res.message);
      } catch (e) {
         setError(e.message);
      }
      setDeleting(null);
      load();
   };

   return (
      <Container fluid>
         <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="fw-bold mb-0">Quản lý Người dùng</h2>
            <Button onClick={() => openForm()}>+ Thêm người dùng</Button>
         </div>
         {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}
         {info && <Alert variant="info" dismissible onClose={() => setInfo("")}>{info}</Alert>}
         <Form.Control className="mb-3" style={{ maxWidth: 300 }} placeholder="Tìm kiếm người dùng..." value={search} onChange={(e) => setSearch(e.target.value)} />

         <Table bordered hover responsive className="bg-white align-middle">
            <thead>
               <tr><th>ID</th><th>Username</th><th>Họ tên</th><th>Role</th><th>Trạng thái</th><th style={{ width: 160 }}>Thao tác</th></tr>
            </thead>
            <tbody>
               {filtered.map((u) => (
                  <tr key={u.id}>
                     <td>{u.id}</td>
                     <td>{u.username}</td>
                     <td>{u.name}</td>
                     <td>{ROLE_LABEL[u.role]}</td>
                     <td><Badge bg={u.status === 1 ? "success" : "secondary"}>{u.status === 1 ? "Hoạt động" : "Đã khóa"}</Badge></td>
                     <td><div className="d-flex gap-2">
                        <Button size="sm" variant="outline-primary" onClick={() => openForm(u)}>Sửa</Button>
                        <Button size="sm" variant="outline-danger" disabled={u.id === me.id} onClick={() => setDeleting(u)}>Xóa</Button>
                     </div></td>
                  </tr>
               ))}
            </tbody>
         </Table>

         <FormModal show={showForm} onHide={() => setShowForm(false)} title={editing ? "Sửa người dùng" : "Thêm người dùng"} onSubmit={submit} error={formError} size="md">
            <Form.Group className="mb-3">
               <Form.Label>Username *</Form.Label>
               <Form.Control value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
            </Form.Group>
            <Form.Group className="mb-3">
               <Form.Label>{editing ? "Mật khẩu mới (bỏ trống nếu không đổi)" : "Mật khẩu *"}</Form.Label>
               <Form.Control type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </Form.Group>
            <Form.Group className="mb-3">
               <Form.Label>Họ và tên *</Form.Label>
               <Form.Control value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </Form.Group>
            <Form.Group className="mb-3">
               <Form.Label>Role</Form.Label>
               <Form.Select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                  {Object.entries(ROLE_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
               </Form.Select>
            </Form.Group>
            <Form.Group>
               <Form.Label>Trạng thái</Form.Label>
               <Form.Select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value={1}>Hoạt động</option>
                  <option value={0}>Khóa</option>
               </Form.Select>
            </Form.Group>
         </FormModal>

         <ConfirmModal
            show={!!deleting}
            onHide={() => setDeleting(null)}
            onConfirm={confirmDelete}
            title="Xóa người dùng"
            message={`Xóa "${deleting?.username}"? Nếu tài khoản đã có bài viết hoặc lịch sử, hệ thống sẽ khóa thay vì xóa.`}
            confirmText="Đồng ý"
         />
      </Container>
   );
}
