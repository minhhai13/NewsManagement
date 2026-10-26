import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Alert, Tabs, Tab, Form } from "react-bootstrap";
import { api } from "../api";
import { formatDate } from "../constants";
import StatusBadge from "../components/StatusBadge";
import ArticleViewModal from "../components/ArticleViewModal";
import ConfirmModal from "../components/ConfirmModal";

/** Editor/Admin: xem bài đã duyệt (hẹn giờ), đã đăng, đã gỡ và gỡ bài (Archive). */
export default function EditorArticles() {
   const [tab, setTab] = useState("PUBLISHED");
   const [articles, setArticles] = useState([]);
   const [error, setError] = useState("");
   const [viewing, setViewing] = useState(null);
   const [archiving, setArchiving] = useState(null);
   const [reason, setReason] = useState("");
   const [dialogError, setDialogError] = useState("");

   const load = useCallback(async () => {
      try {
         setArticles(await api(`/editorial/articles?status=${tab}`));
      } catch (e) {
         setError(e.message);
      }
   }, [tab]);

   useEffect(() => {
      load();
   }, [load]);

   const confirmArchive = async () => {
      try {
         await api(`/editorial/${archiving.id}/archive`, { method: "POST", body: { reason } });
         setArchiving(null);
         load();
      } catch (e) {
         setDialogError(e.message);
      }
   };

   return (
      <Container fluid>
         <h2 className="fw-bold mb-4">Bài đã duyệt / đã đăng</h2>
         {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}
         <Tabs activeKey={tab} onSelect={(k) => { setError(""); setTab(k); }} className="mb-3">
            <Tab eventKey="PUBLISHED" title="Đã đăng" />
            <Tab eventKey="APPROVED_SCHEDULED" title="Đã duyệt - hẹn giờ" />
            <Tab eventKey="ARCHIVED" title="Đã gỡ" />
         </Tabs>

         <Table bordered hover responsive className="bg-white align-middle">
            <thead>
               <tr><th>Tiêu đề</th><th>Tác giả</th><th>Trạng thái</th><th>Hẹn giờ</th><th>Đăng lúc</th><th style={{ width: 200 }}>Thao tác</th></tr>
            </thead>
            <tbody>
               {articles.length === 0 && <tr><td colSpan={6} className="text-center text-secondary">Không có bài nào.</td></tr>}
               {articles.map((a) => (
                  <tr key={a.id}>
                     <td>{a.title}</td>
                     <td>{a.authorName}</td>
                     <td><StatusBadge status={a.status} /></td>
                     <td>{formatDate(a.scheduledPublishDate)}</td>
                     <td>{formatDate(a.publishedAt)}</td>
                     <td><div className="d-flex flex-wrap gap-2">
                        <Button size="sm" variant="outline-dark" onClick={() => setViewing(a)}>Xem</Button>
                        {(a.status === "PUBLISHED" || a.status === "APPROVED_SCHEDULED") && (
                           <Button size="sm" variant="outline-danger" onClick={() => { setArchiving(a); setReason(""); setDialogError(""); }}>
                              Gỡ bài
                           </Button>
                        )}
                     </div></td>
                  </tr>
               ))}
            </tbody>
         </Table>

         <ArticleViewModal article={viewing} onHide={() => setViewing(null)} />

         <ConfirmModal
            show={!!archiving}
            onHide={() => setArchiving(null)}
            onConfirm={confirmArchive}
            title={`Gỡ bài: ${archiving?.title ?? ""}`}
            message="Bài sẽ không còn hiển thị công khai nhưng vẫn được lưu trong hệ thống."
            confirmText="Gỡ bài"
            error={dialogError}
         >
            <Form.Control placeholder="Lý do (không bắt buộc)" value={reason} onChange={(e) => setReason(e.target.value)} />
         </ConfirmModal>
      </Container>
   );
}
