import {Link} from "react-router-dom";

export const EditButton = ({type, id}: {type: string, id: string}) => (
    <Link className="p-button p-button-success" to={`/${type}/${id}`} css={{
        textDecoration: 'none',
        whiteSpace: 'nowrap',
    }}>
        <i className="pi pi-eye"></i>
    </Link>
)
