import { Link } from "react-router";

export default function NotFound() {
   return (
      <div className="text-center mt-5">
         <h2 className="fw-bold">404 - Không tìm thấy trang</h2>
         <Link to="/">Về trang chính</Link>
      </div>
   );
}
