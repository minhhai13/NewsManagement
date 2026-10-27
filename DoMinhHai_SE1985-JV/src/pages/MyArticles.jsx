import { useCallback, useEffect, useState } from "react";
import { Container, Table, Button, Alert } from "react-bootstrap";
import { api } from "../api";
import { formatDate } from "../constants";
import StatusBadge from "../components/StatusBadge";
import ArticleForm from "../components/ArticleForm";
import ArticleViewModal from "../components/ArticleViewModal";

const EDITABLE = ["DRAFT", "CHANGES_REQUESTED"];

export default function MyArticles() {
    const [articles, setArticles] = useState([]);
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState("");
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState(null);
    const [viewing, setViewing] = useState(null);

    const load = useCallback(async () => {
        try {
            setArticles(await api("/news/my"));
            window.dispatchEvent(new Event("articles-changed")); // làm mới badge thông báo ở sidebar
        } catch (e) {
            setError(e.message);
        }
    }, []);

    useEffect(() => {
        load();
        api("/categories").then(setCategories).catch((e) => setError(e.message));
    }, [load]);

    const openForm = (article = null) => {
        setEditing(article);
        setShowForm(true);
    };

    const sendForReview = async (article) => {
        setError("");
        try {
            const path = article.status === "CHANGES_REQUESTED" ? "resubmit" : "submit";
            await api(`/news/${article.id}/${path}`, { method: "POST" });
        } catch (e) {
            setError(e.message);
        }
        await load();
    };

    const handleSaved = async (saved, submitAfter) => {
        if (submitAfter) {
            const path = saved.status === "CHANGES_REQUESTED" ? "resubmit" : "submit";
            try {
                await api(`/news/${saved.id}/${path}`, { method: "POST" });
            } catch (e) {
                await load(); // bài đã được lưu; lỗi (vd. thiếu nội dung) hiện ngay trong form
                throw e;
            }
        }
        setShowForm(false);
        await load();
    };

    const needChanges = articles.filter((a) => a.status === "CHANGES_REQUESTED");

    return (
        <Container fluid>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="fw-bold mb-0">Bài của tôi</h2>
                <Button onClick={() => openForm()}>+ Viết bài mới</Button>
            </div>

            {error && <Alert variant="danger" dismissible onClose={() => setError("")}>{error}</Alert>}
            {needChanges.length > 0 && (
                <Alert variant="warning">
                    Bạn có <strong>{needChanges.length}</strong> bài được Editor yêu cầu sửa. Mở bài để xem phản hồi.
                </Alert>
            )}

            <Table bordered hover responsive className="bg-white align-middle">
                <thead>
                <tr><th>Tiêu đề</th><th>Danh mục</th><th>Trạng thái</th><th>Cập nhật</th><th style={{ width: 260 }}>Thao tác</th></tr>
                </thead>
                <tbody>
                {articles.length === 0 && <tr><td colSpan={5} className="text-center text-secondary">Chưa có bài nào.</td></tr>}
                {articles.map((a) => (
                    <tr key={a.id}>
                        <td>
                            {a.title}
                            {a.status === "CHANGES_REQUESTED" && a.editorFeedback && (
                                <div className="small text-danger mt-1">Phản hồi: {a.editorFeedback}</div>
                            )}
                        </td>
                        <td>{a.categoryName ?? "-"}</td>
                        <td><StatusBadge status={a.status} /></td>
                        <td>{formatDate(a.updatedAt)}</td>
                        <td><div className="d-flex flex-wrap gap-2">
                            <Button size="sm" variant="outline-dark" onClick={() => setViewing(a)}>Xem</Button>
                            {EDITABLE.includes(a.status) && (
                                <>
                                    <Button size="sm" variant="outline-primary" onClick={() => openForm(a)}>Sửa</Button>
                                    <Button size="sm" variant="success" onClick={() => sendForReview(a)}>
                                        {a.status === "CHANGES_REQUESTED" ? "Gửi lại" : "Gửi duyệt"}
                                    </Button>
                                </>
                            )}
                        </div></td>
                    </tr>
                ))}
                </tbody>
            </Table>

            <ArticleForm show={showForm} article={editing} categories={categories} onHide={() => setShowForm(false)} onSaved={handleSaved} />
            <ArticleViewModal article={viewing} onHide={() => setViewing(null)} />
        </Container>
    );
}
