import {BBox2d, Feature, FeatureCollection} from "@turf/helpers/dist/js/lib/geojson"
import {MultiPolygon} from 'geojson'
import {LatLng} from 'leaflet'
import center from '@turf/center'
import {geoJsonPositionToLeaflet} from '../geometry'
import {featureCollection} from "@turf/helpers"
import {chunk} from "lodash"

export type BuurtFeatureCollection = FeatureCollection<MultiPolygon, BuurtProperties>
export type Buurt = Feature<MultiPolygon, BuurtProperties>

// export interface Buurt {
//     type: 'Feature',
//     id: string // e.g. buurten.c443da6a-ae59-4cc3-b0a1-3e426a8eaa2b
//     properties: BuurtProperties,
//     bbox: BBox2d,
//     geometry: MultiPolygon,
// }

export async function fetchBuurtList(gemeente: string): Promise<string[]> {
    if (!gemeente) {
        throw Error('No gemeente to fetch buurten for')
    }

    const params = new URLSearchParams({
        request: 'GetFeature',
        typeName: 'buurten',
        service: 'WFS',
        version: '2.0.0',
        propertyName: 'buurtnaam',
        // We're using XML output here because it has the side-effect of not returning the geometry.
        // This makes the response much smaller.
        //outputFormat: 'json',
        filter: `
            <Filter>
                <AND>
                    <PropertyIsEqualTo>
                        <PropertyName>gemeentenaam</PropertyName>
                        <Literal>${gemeente}</Literal>
                    </PropertyIsEqualTo>
                    <PropertyIsEqualTo>
                        <PropertyName>water</PropertyName>
                        <Literal>NEE</Literal>
                    </PropertyIsEqualTo>
                </AND>
            </Filter>
        `,
    })

    const url = 'https://service.pdok.nl/cbs/wijkenbuurten/2022/wfs/v1_0?' + params.toString()
    const response = await fetch(url)
    if (response.status != 200) {
        throw Error('Failure getting buurten list')
    }

    const body = await response.text()
    const document = new DOMParser().parseFromString(body, 'text/xml')
    const elements = document.getElementsByTagName('wijkenbuurten:buurtnaam')
    const buurten: string[] = Array.prototype.map.call(elements, (element: Element) => {
        return element.textContent
    }) as string[]

    return buurten.sort()
}

// https://service.pdok.nl/cbs/wijkenbuurten/2022/wfs/v1_0?request=DescribeFeatureType&service=WFS&version=2.0.0&typeName=buurten
export interface BuurtProperties {
    buurtcode: string
    buurtnaam: string
    wijkcode: string
    gemeentecode: string
    gemeentenaam: string
    indelingswijzigingWijkenEnBuurten: number
    water: string
    meestVoorkomendePostcode: string
    dekkingspercentage: number
    omgevingsadressendichtheid: number
    stedelijkheidAdressenPerKm2: number
    bevolkingsdichtheidInwonersPerKm2: number
    aantalInwoners: number
    mannen: number
    vrouwen: number
    percentagePersonen0Tot15Jaar: number
    percentagePersonen15Tot25Jaar: number
    percentagePersonen25Tot45Jaar: number
    percentagePersonen45Tot65Jaar: number
    percentagePersonen65JaarEnOuder: number
    percentageOngehuwd: number
    percentageGehuwd: number
    percentageGescheid: number
    percentageVerweduwd: number
    aantalHuishoudens: number
    percentageEenpersoonshuishoudens: number
    percentageHuishoudensZonderKinderen: number
    percentageHuishoudensMetKinderen: number
    gemiddeldeHuishoudsgrootte: number
    percentageWesterseMigratieachtergrond: number
    percentageNietWesterseMigratieachtergrond: number
    percentageUitMarokko: number
    percentageUitNederlandseAntillenEnAruba: number
    percentageUitSuriname: number
    percentageUitTurkije: number
    percentageOverigeNietwestersemigratieachtergrond: number
    oppervlakteTotaalInHa: number
    oppervlakteLandInHa: number
    oppervlakteWaterInHa: number
    jrstatcode: string
    jaar: number
}

export function getBuurtCenter(buurt: Buurt): LatLng {
    const feature = center(buurt.geometry)

    return geoJsonPositionToLeaflet(feature.geometry.coordinates)
}

export async function fetchBuurt(gemeente: string, buurt: string): Promise<Buurt> {
    const buurten = await fetchBuurtenGet(`
        <Filter>
            <AND>
                <PropertyIsEqualTo>
                    <PropertyName>buurtnaam</PropertyName>
                    <Literal>${buurt}</Literal>
                </PropertyIsEqualTo>
                <PropertyIsEqualTo>
                    <PropertyName>gemeentenaam</PropertyName>
                    <Literal>${gemeente}</Literal>
                </PropertyIsEqualTo>
                <PropertyIsEqualTo>
                    <PropertyName>water</PropertyName>
                    <Literal>NEE</Literal>
                </PropertyIsEqualTo>
            </AND>
        </Filter>
    `)

    if (buurten.features.length != 1) {
        throw Error('Expected exactly one buurt')
    }

    return buurten.features[0]
}

export async function fetchBuurtenByCodes(buurtCodes: readonly string[]): Promise<BuurtFeatureCollection> {
    if (buurtCodes.length == 0) {
        return featureCollection([])
    }

    if (buurtCodes.length == 1) {
        return await fetchBuurtenPost(`
            <Filter>
                <PropertyIsEqualTo>
                    <PropertyName>buurtcode</PropertyName>
                    <Literal>${buurtCodes[0]}</Literal>
                </PropertyIsEqualTo>
            </Filter>
    `)
    }

    // There is a limit to the number of filter clauses on the server
    const buurtCodeChunks = chunk(buurtCodes, 10)

    const requests = buurtCodeChunks.map(buurtCodeChunk =>
        fetchBuurtenPost(`
            <Filter>
                <Or>
                    ${buurtCodes.map(code => `
                        <PropertyIsEqualTo>
                            <PropertyName>buurtcode</PropertyName>
                            <Literal>${code}</Literal>
                        </PropertyIsEqualTo>
                    `).join("").replaceAll(/ +/g, " ").replaceAll("\n", "")}
                </Or>
            </Filter>
        `,
        ))

    const featureCollections = await Promise.all(requests)

    return featureCollection(featureCollections.flatMap(fc => fc.features))
}

const baseUrl = "https://service.pdok.nl/cbs/wijkenbuurten/2023/wfs/v1_0"

async function fetchBuurtenGet(xmlFilter: string): Promise<BuurtFeatureCollection> {
    const params = new URLSearchParams({
        request: 'GetFeature',
        typeName: 'buurten',
        service: 'WFS',
        version: '2.0.0',
        outputFormat: 'json',
        srsName: 'EPSG:4326', // output coordinate system
        filter: xmlFilter,
    })

    const url = baseUrl + "?" + params.toString()
    const response = await fetch(url)
    if (response.status != 200) {
        throw Error('Failure getting buurt geometry')
    }

    return await response.json()
}

async function fetchBuurtenPost(xmlFilter: string): Promise<BuurtFeatureCollection> {
    const response = await fetch(baseUrl, {
        method: "POST",
        body: `
            <GetFeature service="WFS" version="2.0.0" outputFormat="json" xmlns="http://www.opengis.net/wfs/2.0">
                <Query typeName="buurten" srsName="EPSG:4326">
                    ${xmlFilter}
                </Query>
            </GetFeature>
        `,
    })

    if (response.status != 200) {
        throw Error("Failure getting buurt geometry")
    }

    return await response.json()
}


