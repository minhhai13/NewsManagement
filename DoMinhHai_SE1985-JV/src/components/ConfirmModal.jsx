import { Modal, Button, Alert } from "react-bootstrap";

export default function ConfirmModal({ show, onHide, onConfirm, title, message, confirmText = "Đồng ý", variant = "danger", error, children }) {
   return (
      <Modal show={show} onHide={onHide} centered backdrop="static">
         <Modal.Header closeButton>
            <Modal.Title className="fw-bold fs-5">{title || "Xác nhận"}</Modal.Title>
         </Modal.Header>
         <Modal.Body>
            {error && <Alert variant="danger">{error}</Alert>}
            {message && <p className="mb-2">{message}</p>}
            {children}
         </Modal.Body>
         <Modal.Footer>
            <Button variant="outline-secondary" onClick={onHide}>Hủy bỏ</Button>
            <Button variant={variant} onClick={onConfirm}>{confirmText}</Button>
         </Modal.Footer>
      </Modal>
   );
}
