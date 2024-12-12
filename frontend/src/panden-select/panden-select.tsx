import {FunctionComponent} from "react"
import {BuurtFeatureCollection, getBuurtCenter} from "../services/wijkenbuurten/buurten"
import {Bag2DPand} from "../services/bag2d"
import {GeoJSON, MapContainer, TileLayer} from "react-leaflet"
import {featureCollection} from "@turf/helpers"
import {geoJsonPositionToLeaflet} from "../services/geometry"
import center from "@turf/center"
import {LeafletMouseEventHandlerFn} from "leaflet"
import {PandID} from "zero-zummon"
import {map, reduce} from "../services/iterable"
import {assertDefined} from "../services/util"

/**
 * Component which renders a map from data already loaded from the network.
 */
export const PandenSelect: FunctionComponent<{
    buurten: BuurtFeatureCollection,
    panden: Map<BigInt, Bag2DPand>,
    otherCompaniesPandIds: string[],
    thisCompanyPandIds: ReadonlySet<PandID>,
    addThisCompanyPandId: (pandId: PandID) => void,
    removeThisCompanyPandId: (pandId: PandID) => void,
}> = ({ buurten, panden, otherCompaniesPandIds, thisCompanyPandIds, addThisCompanyPandId, removeThisCompanyPandId }) => {

    const buurtCenter = geoJsonPositionToLeaflet(center(buurten).geometry.coordinates)

    const addPand: LeafletMouseEventHandlerFn = (event) => {

        const pandFeature: Bag2DPand = event.propagatedFrom.feature
        const pandId = new PandID(pandFeature.properties.identificatie)

        addThisCompanyPandId(pandId)
    }

    const removePand: LeafletMouseEventHandlerFn = (event) => {

        const pandFeature: Bag2DPand = event.propagatedFrom.feature
        const pandId = new PandID(pandFeature.properties.identificatie)

        removeThisCompanyPandId(pandId)
    }

    const selectedPanden = featureCollection(
        Array.from(
            map(
                thisCompanyPandIds,
                pandId => assertDefined(panden.get(BigInt(pandId.value)))
            )
        )
    )

    return (
        <MapContainer
            center={buurtCenter}
            zoom={15}
            scrollWheelZoom={true}
            style={{
                minHeight: "30rem",
                flexGrow: 1,
            }}
        >
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                // TODO: option to use BAG WMS tiles
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <GeoJSON data={buurten} pathOptions={{color: 'salmon'}} />
            <GeoJSON
                data={featureCollection(Array.from(panden.values()))}
                pathOptions={{color: 'blue'}}
                eventHandlers={{click: addPand}}/>

            <GeoJSON
                key={reduce(thisCompanyPandIds, (acc, val) => acc + val.value, "")}
                data={selectedPanden}
                pathOptions={{color: 'red'}}
                eventHandlers={{click: removePand}} />
        </MapContainer>
    )
}