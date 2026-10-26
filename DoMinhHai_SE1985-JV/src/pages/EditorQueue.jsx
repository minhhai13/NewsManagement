import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Alert, Form } from "react-bootstrap";
import { api } from "../api";
import { formatDate, localInputToIso } from "../constants";
import ArticleViewModal from "../components/ArticleViewModal";
import ConfirmModal from "../components/ConfirmModal";

/** Hàng chờ duyệt. Mỗi thao tác tối đa 3 lần bấm: nút ở dòng -> (nhập) -> xác nhận. */
export default function EditorQueue() {
   const [queue, setQueue] = useState([]);
   const [error, setError] = useState("");
   const [viewing, setViewing] = useState(null);
   const [approving, setApproving] = useState(null);
   const [mode, setMode] = useState("now"); // now | schedule
   const [when, setWhen] = useState("");
   const [rejecting, setRejecting] = useState(null);
   const [feedback, setFeedback] = useState("");
   const [dialogError, setDialogError] = useState("");

   const load = useCallback(async () => {
      try {
         setQueue(await api("/editorial/queue"));
      } catch (e) {
         setError(e.message);
      }
   }, []);

   useEffect(() => {
      load();
   }, [load]);

   const openApprove = (a) => {
      setApproving(a);
      setMode("now");
      setWhen("");
      setDialogError("");
   };

   const openReject = (a) => {
      setRejecting(a);
      setFeedback("");
      setDialogError("");
   };

   const confirmApprove = async () => {
      if (mode === "schedule" && !when) {
         setDialogError("Hãy chọn giờ hẹn");
         return;
      }
      try {
         await api(`/editorial/${approving.id}/approve`, {
            method: "POST",
            body: { scheduledPublishDate: mode === "schedule" ? localInputToIso(when) : null },
         });
         setApproving(null);
         load();
      } catch (e) {
         setDialogError(e.message);
      }
   };

   const confirmReject = async () => {
      try {
         await api(`/editorial/${rejecting.id}/request-changes`, { method: "POST", body: { feedback } });
         setRejecting(null);
         load();
      } catch (e) {
         setDialogError(e.message);
      }
   };

   return (
      <Container fluid>
         <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="fw-bold mb-0">Hàng chờ duyệt</h2>
            <Button variant="outline-dark" onClick={load}>Làm mới</Button>
         </div>
         {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}

         <Table bordered hover responsive className="bg-white align-middle">
            <thead>
               <tr><th>Tiêu đề</th><th>Tác giả</th><th>Danh mục</th><th>Gửi lúc</th><th style={{ width: 280 }}>Thao tác</th></tr>
            </thead>
            <tbody>
               {queue.length === 0 && <tr><td colSpan={5} className="text-center text-secondary">Không có bài nào chờ duyệt.</td></tr>}
               {queue.map((a) => (
                  <tr key={a.id}>
                     <td>{a.title}</td>
                     <td>{a.authorName}</td>
                     <td>{a.categoryName}</td>
                     <td>{formatDate(a.submittedAt)}</td>
                     <td><div className="d-flex flex-wrap gap-2">
                        <Button size="sm" variant="outline-dark" onClick={() => setViewing(a)}>Xem</Button>
                        <Button size="sm" variant="success" onClick={() => openApprove(a)}>Duyệt</Button>
                        <Button size="sm" variant="outline-danger" onClick={() => openReject(a)}>Yêu cầu sửa</Button>
                     </div></td>
                  </tr>
               ))}
            </tbody>
         </Table>

         <ArticleViewModal article={viewing} onHide={() => setViewing(null)} />

         <ConfirmModal
            show={!!approving}
            onHide={() => setApproving(null)}
            onConfirm={confirmApprove}
            title={`Duyệt: ${approving?.title ?? ""}`}
            confirmText="Xác nhận duyệt"
            variant="success"
            error={dialogError}
         >
            <Form.Check type="radio" id="m-now" label="Đăng ngay" checked={mode === "now"} onChange={() => setMode("now")} />
            <Form.Check type="radio" id="m-sch" label="Hẹn giờ đăng" checked={mode === "schedule"} onChange={() => setMode("schedule")} />
            {mode === "schedule" && (
               <Form.Control type="datetime-local" className="mt-2" value={when} onChange={(e) => setWhen(e.target.value)} />
            )}
         </ConfirmModal>

         <ConfirmModal
            show={!!rejecting}
            onHide={() => setRejecting(null)}
            onConfirm={confirmReject}
            title={`Yêu cầu sửa: ${rejecting?.title ?? ""}`}
            confirmText="Gửi phản hồi"
            error={dialogError}
         >
            <Form.Label>Lý do / nội dung cần sửa (bắt buộc)</Form.Label>
            <Form.Control as="textarea" rows={4} value={feedback} onChange={(e) => setFeedback(e.target.value)} autoFocus />
         </ConfirmModal>
      </Container>
   );
}
