import {UseFormReturn} from "react-hook-form"
import {FunctionComponent} from "react"
import {PandenSelectLoader} from "../../panden-select/panden-select-loader"
import {PandID} from "zero-zummon"

/**
 * The form uses the raw JSON values, where panden are a string array.
 * The PandenSelect component it is a Set of PandID objects.
 * This component glues it together.
 */
export const PandenSelect: FunctionComponent<{
    buurtcodes: readonly string[],
    form: UseFormReturn,
    prefix: string
}> = ({
    buurtcodes,
    form,
    prefix,
}) => {
    const name = `${prefix}.pandIds`
    const pandIdStrings = form.watch(name, []) as string[]
    if (buurtcodes.length === 0) {
        return null
    }

    const pandIdObjects = new Set(pandIdStrings.map(id => new PandID(id)))

    return (
        <>
            <h2>2. Pand</h2>
            {/* improvement: display this message only when the user has indicated there are multiple grid connections */}
            <p>Selecteer het pand of de panden die horen bij deze netaansluiting</p>
            <PandenSelectLoader
                buurtcodes={buurtcodes}
                thisCompanyPandIds={pandIdObjects}
                addThisCompanyPandId={(pandId: PandID) => {
                    const stringSet = new Set(pandIdStrings)
                    stringSet.add(pandId.value)
                    form.setValue(name, stringSet.values().toArray())
                }}
                removeThisCompanyPandId={(pandId: PandID) => {
                    const stringSet = new Set(pandIdStrings)
                    stringSet.delete(pandId.value)
                    form.setValue(name, stringSet.values().toArray())
                }} />
        </>
    )
}
