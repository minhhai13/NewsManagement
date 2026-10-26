import { Modal, Button, Alert } from "react-bootstrap";

export default function FormModal({ show, onHide, title, onSubmit, children, submitText = "Lưu lại", error, busy, size = "lg", footerExtra }) {
   return (
      <Modal show={show} onHide={onHide} centered backdrop="static" size={size}>
         <Modal.Header closeButton>
            <Modal.Title className="fw-bold fs-5">{title}</Modal.Title>
         </Modal.Header>
         <Modal.Body>
            {error && <Alert variant="danger">{error}</Alert>}
            {children}
         </Modal.Body>
         <Modal.Footer>
            <Button variant="outline-secondary" onClick={onHide}>Đóng</Button>
            {footerExtra}
            {onSubmit && (
               <Button variant="primary" onClick={onSubmit} disabled={busy}>{submitText}</Button>
            )}
         </Modal.Footer>
      </Modal>
   );
}
