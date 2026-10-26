import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Form, Badge, Alert } from "react-bootstrap";
import { api } from "../api";
import FormModal from "../components/FormModal";
import ConfirmModal from "../components/ConfirmModal";

const EMPTY = { name: "", description: "", status: 1, parentCategoryId: "" };

export default function CategoryManage() {
   const [categories, setCategories] = useState([]);
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
         setCategories(await api("/categories"));
      } catch (e) {
         setError(e.message);
      }
   }, []);

   useEffect(() => {
      load();
   }, [load]);

   const nameOf = (id) => categories.find((c) => c.id === id)?.name ?? "-";
   const filtered = categories.filter((c) => c.name.toLowerCase().includes(search.toLowerCase()));

   const openForm = (item = null) => {
      setEditing(item);
      setFormError("");
      setForm(item
         ? { name: item.name, description: item.description ?? "", status: item.status, parentCategoryId: item.parentCategoryId ?? "" }
         : EMPTY);
      setShowForm(true);
   };

   const submit = async () => {
      try {
         const body = { ...form, status: Number(form.status), parentCategoryId: form.parentCategoryId ? Number(form.parentCategoryId) : null };
         if (editing) await api(`/categories/${editing.id}`, { method: "PUT", body });
         else await api("/categories", { method: "POST", body });
         setShowForm(false);
         load();
      } catch (e) {
         setFormError(e.message);
      }
   };

   const confirmDelete = async () => {
      try {
         const res = await api(`/categories/${deleting.id}`, { method: "DELETE" });
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
            <h2 className="fw-bold mb-0">Quản lý Danh mục</h2>
            <Button onClick={() => openForm()}>+ Thêm danh mục</Button>
         </div>
         {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}
         {info && <Alert variant="info" dismissible onClose={() => setInfo("")}>{info}</Alert>}
         <Form.Control className="mb-3" style={{ maxWidth: 300 }} placeholder="Tìm kiếm danh mục..." value={search} onChange={(e) => setSearch(e.target.value)} />

         <Table bordered hover responsive className="bg-white align-middle">
            <thead>
               <tr><th>ID</th><th>Tên</th><th>Mô tả</th><th>Danh mục cha</th><th>Trạng thái</th><th style={{ width: 160 }}>Thao tác</th></tr>
            </thead>
            <tbody>
               {filtered.map((c) => (
                  <tr key={c.id}>
                     <td>{c.id}</td>
                     <td>{c.name}</td>
                     <td>{c.description}</td>
                     <td>{c.parentCategoryId ? nameOf(c.parentCategoryId) : "-"}</td>
                     <td><Badge bg={c.status === 1 ? "success" : "secondary"}>{c.status === 1 ? "Hoạt động" : "Vô hiệu hóa"}</Badge></td>
                     <td><div className="d-flex gap-2">
                        <Button size="sm" variant="outline-primary" onClick={() => openForm(c)}>Sửa</Button>
                        <Button size="sm" variant="outline-danger" onClick={() => setDeleting(c)}>Xóa</Button>
                     </div></td>
                  </tr>
               ))}
            </tbody>
         </Table>

         <FormModal show={showForm} onHide={() => setShowForm(false)} title={editing ? "Sửa danh mục" : "Thêm danh mục"} onSubmit={submit} error={formError} size="md">
            <Form.Group className="mb-3">
               <Form.Label>Tên *</Form.Label>
               <Form.Control value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </Form.Group>
            <Form.Group className="mb-3">
               <Form.Label>Mô tả</Form.Label>
               <Form.Control as="textarea" rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </Form.Group>
            <Form.Group className="mb-3">
               <Form.Label>Danh mục cha</Form.Label>
               <Form.Select value={form.parentCategoryId} onChange={(e) => setForm({ ...form, parentCategoryId: e.target.value })}>
                  <option value="">-- Không có --</option>
                  {categories.filter((c) => c.id !== editing?.id).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
               </Form.Select>
            </Form.Group>
            <Form.Group>
               <Form.Label>Trạng thái</Form.Label>
               <Form.Select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value={1}>Hoạt động</option>
                  <option value={0}>Vô hiệu hóa</option>
               </Form.Select>
            </Form.Group>
         </FormModal>

         <ConfirmModal
            show={!!deleting}
            onHide={() => setDeleting(null)}
            onConfirm={confirmDelete}
            title="Xóa danh mục"
            message={`Xóa "${deleting?.name}"? Nếu danh mục đang được dùng, hệ thống sẽ vô hiệu hóa thay vì xóa.`}
            confirmText="Đồng ý"
         />
      </Container>
   );
}
