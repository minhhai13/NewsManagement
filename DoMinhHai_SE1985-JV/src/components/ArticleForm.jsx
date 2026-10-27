import { useEffect, useState } from "react";
import { Form, Button } from "react-bootstrap";
import { api } from "../api";
import FormModal from "./FormModal";

const EMPTY = { title: "", headline: "", content: "", tags: "", categoryId: "" };

/**
 * Form tạo/sửa bài của Contributor. Không có status/author: backend tự gán.
 * onSaved(article, submitAfter) được gọi sau khi lưu thành công.
 */
export default function ArticleForm({ show, article, categories, onHide, onSaved }) {
    const [form, setForm] = useState(EMPTY);
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    // Bài đang soạn trong form: sau lần lưu đầu tiên của bài mới, các lần lưu tiếp theo phải là sửa (PUT), không tạo trùng.
    const [current, setCurrent] = useState(article);

    useEffect(() => {
        if (!show) return;
        setError("");
        setCurrent(article);
        setForm(article
            ? {
                title: article.title ?? "",
                headline: article.headline ?? "",
                content: article.content ?? "",
                tags: article.tags ?? "",
                categoryId: article.categoryId ?? "",
            }
            : EMPTY);
    }, [show, article]);

    const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

    const save = async (submitAfter) => {
        setError("");
        setBusy(true);
        try {
            const body = {
                ...form,
                categoryId: form.categoryId ? Number(form.categoryId) : null,
                version: current?.version ?? null,
            };
            const saved = current
                ? await api(`/news/${current.id}`, { method: "PUT", body })
                : await api("/news", { method: "POST", body });
            setCurrent(saved);
            await onSaved(saved, submitAfter);
        } catch (e) {
            setError(e.message);
        } finally {
            setBusy(false);
        }
    };

    const isResubmit = current?.status === "CHANGES_REQUESTED";

    return (
        <FormModal
            show={show}
            onHide={onHide}
            title={current ? "Sửa bài viết" : "Viết bài mới"}
            onSubmit={() => save(false)}
            submitText="Lưu"
            error={error}
            busy={busy}
            footerExtra={
                <Button variant="success" disabled={busy} onClick={() => save(true)}>
                    {isResubmit ? "Lưu & gửi lại" : "Lưu & gửi duyệt"}
                </Button>
            }
        >
            {isResubmit && current.editorFeedback && (
                <div className="alert alert-danger"><strong>Phản hồi của Editor:</strong> {current.editorFeedback}</div>
            )}
            <Form.Group className="mb-3">
                <Form.Label>Tiêu đề *</Form.Label>
                <Form.Control value={form.title} onChange={set("title")} />
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Tóm tắt (headline)</Form.Label>
                <Form.Control value={form.headline} onChange={set("headline")} />
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Danh mục (bắt buộc khi gửi duyệt)</Form.Label>
                <Form.Select value={form.categoryId} onChange={set("categoryId")}>
                    <option value="">-- Chọn danh mục --</option>
                    {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Nội dung (bắt buộc khi gửi duyệt)</Form.Label>
                <Form.Control as="textarea" rows={8} value={form.content} onChange={set("content")} />
            </Form.Group>
            <Form.Group>
                <Form.Label>Tags</Form.Label>
                <Form.Control value={form.tags} onChange={set("tags")} placeholder="vd: thi cuối kỳ, thông báo" />
            </Form.Group>
        </FormModal>
    );
}
