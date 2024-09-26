import {usePromise} from "../hooks/use-promise"
import {FunctionComponent} from "react"
import {Project} from "zero-zummon"
import {fetchBuurtenByCodes} from "../services/wijkenbuurten/buurten"
import {fetchBag2dPanden} from "../services/bag2d"
import {geometryToBoundingBox} from "../services/geometry"
import {PandenSelect} from "./panden-select"
import {Nullable} from "primereact/ts-helpers"
import {PandID} from "zero-zummon"

const fetchBuurtenAndPanden = async (buurtCodes: string[]) => {
    const buurten = await fetchBuurtenByCodes(buurtCodes)

    const panden = await fetchBag2dPanden(geometryToBoundingBox(buurten))

    return {
        buurten: buurten,
        panden: panden,
    }
}

export const PandenSelectLoader: FunctionComponent<{
    project: Nullable<Project>,
    thisCompanyPandIds: ReadonlySet<PandID>,
    addThisCompanyPandId: (pandId: PandID) => void,
    removeThisCompanyPandId: (pandId: PandID) => void,
}> = ({project, thisCompanyPandIds, addThisCompanyPandId, removeThisCompanyPandId}) => {
    if (!project) {
        return <p>Geen project gevonden</p>
    }

    const buurtCodes = [...project.buurtCodes.asJsReadonlyArrayView()]

    const [result, error, pending] = usePromise(
        async () => fetchBuurtenAndPanden(buurtCodes),
        buurtCodes, // TODO: deps not working properly, keeps re-fetching
    )

    if (error) {
        return <p>{error.toString()}</p>
    }

    if (pending) {
        return <p>Bezig met laden...</p>
    }

    return <PandenSelect
        buurten={result?.buurten}
        otherCompaniesPandIds={[]}
        panden={result.panden}
        addThisCompanyPandId={addThisCompanyPandId}
        removeThisCompanyPandId={removeThisCompanyPandId}
        thisCompanyPandIds={thisCompanyPandIds} />
}