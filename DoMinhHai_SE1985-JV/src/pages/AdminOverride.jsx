import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Alert, Form } from "react-bootstrap";
import { api } from "../api";
import { STATUS_LABEL, formatDate, localInputToIso } from "../constants";
import StatusBadge from "../components/StatusBadge";
import ArticleViewModal from "../components/ArticleViewModal";
import ConfirmModal from "../components/ConfirmModal";

/** Admin: xử lý ngoại lệ bằng override (bắt buộc lý do, có ghi log). */
export default function AdminOverride() {
   const [status, setStatus] = useState("PENDING_REVIEW");
   const [articles, setArticles] = useState([]);
   const [error, setError] = useState("");
   const [viewing, setViewing] = useState(null);
   const [target, setTarget] = useState(null);
   const [targetStatus, setTargetStatus] = useState("DRAFT");
   const [reason, setReason] = useState("");
   const [when, setWhen] = useState("");
   const [dialogError, setDialogError] = useState("");

   const load = useCallback(async () => {
      try {
         setArticles(await api(`/editorial/articles?status=${status}`));
      } catch (e) {
         setError(e.message);
      }
   }, [status]);

   useEffect(() => {
      load();
   }, [load]);

   const open = (a) => {
      setTarget(a);
      setTargetStatus(Object.keys(STATUS_LABEL).find((s) => s !== a.status));
      setReason("");
      setWhen("");
      setDialogError("");
   };

   const confirm = async () => {
      try {
         await api(`/admin/news/${target.id}/override`, {
            method: "POST",
            body: {
               targetStatus,
               reason,
               scheduledPublishDate: targetStatus === "APPROVED_SCHEDULED" ? localInputToIso(when) : null,
            },
         });
         setTarget(null);
         load();
      } catch (e) {
         setDialogError(e.message);
      }
   };

   return (
      <Container fluid>
         <h2 className="fw-bold mb-4">Bài viết - Admin override</h2>
         <Alert variant="secondary">
            Override là lệnh ngoại lệ: bắt buộc nhập lý do, được ghi vào lịch sử và không thay đổi nội dung bài.
         </Alert>
         {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}

         <Form.Select className="mb-3" style={{ maxWidth: 300 }} value={status} onChange={(e) => { setError(""); setStatus(e.target.value); }}>
            {Object.entries(STATUS_LABEL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
         </Form.Select>

         <Table bordered hover responsive className="bg-white align-middle">
            <thead>
               <tr><th>Tiêu đề</th><th>Tác giả</th><th>Trạng thái</th><th>Cập nhật</th><th style={{ width: 200 }}>Thao tác</th></tr>
            </thead>
            <tbody>
               {articles.length === 0 && <tr><td colSpan={5} className="text-center text-secondary">Không có bài nào.</td></tr>}
               {articles.map((a) => (
                  <tr key={a.id}>
                     <td>{a.title}</td>
                     <td>{a.authorName}</td>
                     <td><StatusBadge status={a.status} /></td>
                     <td>{formatDate(a.updatedAt)}</td>
                     <td><div className="d-flex gap-2">
                        <Button size="sm" variant="outline-dark" onClick={() => setViewing(a)}>Xem</Button>
                        <Button size="sm" variant="warning" onClick={() => open(a)}>Override</Button>
                     </div></td>
                  </tr>
               ))}
            </tbody>
         </Table>

         <ArticleViewModal article={viewing} onHide={() => setViewing(null)} />

         <ConfirmModal
            show={!!target}
            onHide={() => setTarget(null)}
            onConfirm={confirm}
            title={`Override: ${target?.title ?? ""}`}
            confirmText="Thực hiện override"
            variant="warning"
            error={dialogError}
         >
            <Form.Label>Chuyển sang trạng thái</Form.Label>
            <Form.Select className="mb-3" value={targetStatus} onChange={(e) => setTargetStatus(e.target.value)}>
               {Object.entries(STATUS_LABEL).filter(([k]) => k !== target?.status).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </Form.Select>
            {targetStatus === "APPROVED_SCHEDULED" && (
               <Form.Control type="datetime-local" className="mb-3" value={when} onChange={(e) => setWhen(e.target.value)} />
            )}
            <Form.Label>Lý do (bắt buộc)</Form.Label>
            <Form.Control as="textarea" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
         </ConfirmModal>
      </Container>
   );
}
