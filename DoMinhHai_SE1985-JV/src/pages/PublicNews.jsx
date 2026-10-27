import { useEffect, useState } from "react";
import { Container, Card, Row, Col, Form, Alert, Modal, Button, Badge } from "react-bootstrap";
import { Link } from "react-router";
import { api } from "../api";
import { formatDate } from "../constants";
import { useAuth } from "../auth/AuthContext";

/** Reader: không cần đăng nhập, backend chỉ trả bài PUBLISHED. */
export default function PublicNews() {
    const { user } = useAuth();
    const [articles, setArticles] = useState([]);
    const [categories, setCategories] = useState([]);
    const [categoryId, setCategoryId] = useState("");
    const [error, setError] = useState("");
    const [selected, setSelected] = useState(null);

    useEffect(() => {
        api("/categories").then(setCategories).catch((e) => setError(e.message));
    }, []);

    useEffect(() => {
        api(`/public/news${categoryId ? `?categoryId=${categoryId}` : ""}`)
            .then(setArticles)
            .catch((e) => setError(e.message));
    }, [categoryId]);

    return (
        <Container className="py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="fw-bold mb-0">FUNews - Tin tức</h2>
                <Link to={user ? "/" : "/login"}>{user ? "Về trang quản lý" : "Đăng nhập"}</Link>
            </div>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form.Select className="mb-4" style={{ maxWidth: 300 }} value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                <option value="">Tất cả danh mục</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </Form.Select>

            {articles.length === 0 && <p className="text-secondary">Chưa có bài viết nào được đăng.</p>}
            <Row xs={1} md={2} className="g-3">
                {articles.map((a) => (
                    <Col key={a.id}>
                        <Card className="h-100 shadow-sm" role="button" onClick={() => setSelected(a)}>
                            <Card.Body>
                                <Badge bg="primary" className="mb-2">{a.categoryName}</Badge>
                                <Card.Title>{a.title}</Card.Title>
                                <Card.Text className="text-secondary">{a.headline}</Card.Text>
                            </Card.Body>
                            <Card.Footer className="small text-secondary">{a.authorName} · {formatDate(a.publishedAt)}</Card.Footer>
                        </Card>
                    </Col>
                ))}
            </Row>

            <Modal show={!!selected} onHide={() => setSelected(null)} size="lg" centered scrollable>
                {selected && (
                    <>
                        <Modal.Header closeButton><Modal.Title className="fw-bold fs-5">{selected.title}</Modal.Title></Modal.Header>
                        <Modal.Body>
                            <p className="small text-secondary">{selected.categoryName} · {selected.authorName} · {formatDate(selected.publishedAt)}</p>
                            <div className="article-content">{selected.content}</div>
                        </Modal.Body>
                        <Modal.Footer><Button variant="outline-secondary" onClick={() => setSelected(null)}>Đóng</Button></Modal.Footer>
                    </>
                )}
            </Modal>
        </Container>
    );
}
