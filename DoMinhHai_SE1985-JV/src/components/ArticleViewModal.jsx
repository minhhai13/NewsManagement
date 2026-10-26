import { useEffect, useState } from "react";
import { Modal, Button, Alert, Table } from "react-bootstrap";
import { api } from "../api";
import { ACTION_LABEL, STATUS_LABEL, formatDate } from "../constants";
import StatusBadge from "./StatusBadge";

/**
 * Xem một bài kèm lịch sử workflow (audit timeline).
 * `article` là bài cần xem; `detailPath` nếu có thì tải lại chi tiết từ API đó (vd. /editorial/{id}).
 */
export default function ArticleViewModal({ article, onHide }) {
   const [logs, setLogs] = useState([]);
   const [error, setError] = useState("");

   useEffect(() => {
      if (!article) return;
      setLogs([]);
      setError("");
      api(`/news/${article.id}/audit-logs`).then(setLogs).catch((e) => setError(e.message));
   }, [article]);

   if (!article) return null;

   return (
      <Modal show onHide={onHide} centered size="xl" scrollable>
         <Modal.Header closeButton>
            <Modal.Title className="fw-bold fs-5">{article.title}</Modal.Title>
         </Modal.Header>
         <Modal.Body>
            <div className="d-flex flex-wrap gap-3 mb-3 text-secondary small">
               <StatusBadge status={article.status} />
               <span>Tác giả: {article.authorName}</span>
               <span>Danh mục: {article.categoryName ?? "-"}</span>
               <span>Gửi duyệt: {formatDate(article.submittedAt)}</span>
               {article.scheduledPublishDate && <span>Hẹn giờ: {formatDate(article.scheduledPublishDate)}</span>}
               {article.publishedAt && <span>Đăng lúc: {formatDate(article.publishedAt)}</span>}
            </div>
            {article.editorFeedback && article.status === "CHANGES_REQUESTED" && (
               <Alert variant="danger">
                  <strong>Phản hồi của Editor:</strong> {article.editorFeedback}
               </Alert>
            )}
            {article.headline && <p className="fw-semibold">{article.headline}</p>}
            <div className="article-content border rounded bg-white p-3 mb-2">
               {article.content || <em className="text-secondary">(chưa có nội dung)</em>}
            </div>
            {article.tags && <p className="small text-secondary">Tags: {article.tags}</p>}

            <h6 className="mt-4">Lịch sử workflow</h6>
            {error && <Alert variant="warning">{error}</Alert>}
            <Table size="sm" bordered>
               <thead>
                  <tr><th>Thời gian</th><th>Hành động</th><th>Trạng thái</th><th>Người thực hiện</th><th>Ghi chú</th></tr>
               </thead>
               <tbody>
                  {logs.map((l) => (
                     <tr key={l.id}>
                        <td>{formatDate(l.createdAt)}</td>
                        <td>{ACTION_LABEL[l.action] ?? l.action}</td>
                        <td>{l.fromStatus ? `${STATUS_LABEL[l.fromStatus]} → ` : ""}{STATUS_LABEL[l.toStatus]}</td>
                        <td>{l.actorName}</td>
                        <td>{l.comment ?? ""}</td>
                     </tr>
                  ))}
               </tbody>
            </Table>
         </Modal.Body>
         <Modal.Footer>
            <Button variant="outline-secondary" onClick={onHide}>Đóng</Button>
         </Modal.Footer>
      </Modal>
   );
}
