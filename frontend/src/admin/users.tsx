import {FunctionComponent} from "react";
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {useUsers} from "./use-users";
import {PrimeReactProvider} from "primereact/api";
import {User} from "zero-zummon"

import "primereact/resources/themes/lara-light-cyan/theme.css"
import 'primeicons/primeicons.css'
import {DeleteButton} from "./delete-button";
import {EditButton} from "./edit-button";
import {JsonButton} from "./json-button";
import {DeeplinkButton} from "./deeplink-button"
import {ZeroLayout} from "../components/zero-layout"

export const Users: FunctionComponent = () => {
    const {loading, users, changeUser, removeUser} = useUsers()

    return (
        <PrimeReactProvider>
            <ZeroLayout subtitle="Users">
                <DataTable
                    value={users}
                    loading={loading}
                    sortField="created"
                    sortOrder={-1}
                    filterDisplay="row"
                >
                    <Column field="note" header="Note" sortable filter />

                    <Column body={(user: User) => (
                        <div css={{
                            display: 'flex',
                            '> *': {
                                margin: `${1/6}rem`
                            },
                        }}>
                            <JsonButton surveyId={user.id}/>
                            <DeleteButton surveyId={user.id} onDelete={removeUser}/>
                            <EditButton surveyId={user.id}/>
                            <DeeplinkButton surveyId={user.id}/>
                        </div>
                    )}/>
                </DataTable>
            </ZeroLayout>
        </PrimeReactProvider>
    )
}
