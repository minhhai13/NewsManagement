import { Badge } from "react-bootstrap";
import { STATUS_LABEL, STATUS_VARIANT } from "../constants";

export default function StatusBadge({ status }) {
   return <Badge bg={STATUS_VARIANT[status]}>{STATUS_LABEL[status] ?? status}</Badge>;
}
