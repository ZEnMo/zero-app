import {BBox2d} from '@turf/helpers/dist/js/lib/geojson'
import {Feature, Polygon} from "geojson"
import {LatLngBounds} from 'leaflet'
import proj4 from 'proj4'

interface ResponseBody {
    type: 'FeatureCollection',
    // presence of these numbers is finicky
    numberMatched?: number,
    numberReturned?: number,
    name: 'pand',
    features: Bag2DPand[],
    bbox: BBox2d,
}

export type Bag2DPand = Feature<Polygon, Bag2DPandProperties>

// export interface Bag2DPand {
//     type: 'Feature',
//     id: string // e.g. pand.97faabcf-a317-4c15-ae42-d1b8d04beb7d
//     properties: Bag2DPandProperties,
//     bbox: BBox2d,
//     geometry: Polygon,
// }

export interface Bag2DPandProperties {
    identificatie: string, // numeric
    rdf_seealso: string, // url
    bouwjaar: number,
    status: string,
    gebruiksdoel: string,
    aantal_verblijfsobjecten: number,
    oppervlakte_min: number,
    oppervlakte_max: number,
    // incomplete
}

export async function fetchBag2dPanden(boundingBox: LatLngBounds, startIndex = 0): Promise<Map<BigInt, Bag2DPand>> {
    const params = new URLSearchParams({
        request: 'GetFeature',
        service: 'WFS',
        typeName: 'bag:pand',
        srsName: 'EPSG:4326', // output coordinate system
        outputFormat: 'json',
        bbox: [
            boundingBox.getWest(),
            boundingBox.getSouth(),
            boundingBox.getEast(),
            boundingBox.getNorth(),
            'urn:ogc:def:rs:EPSG::4326', // input coordinate system
        ].join(','),
        version: '2.0.0',
        startIndex: startIndex.toString(),
    })

    const url = 'https://service.pdok.nl/lv/bag/wfs/v2_0?' + params.toString()

    const response = await fetch(url)
    if (response.status != 200) {
        throw Error('Failure getting BAG 2D data')
    }

    const json = await response.json() as ResponseBody

    // There seems no consistent indication of whether there are more results.
    // We assume this is the last page if there are less results than below.
    const paginationThreshold = 800

    // Recursively fetch the next page.
    //
    // There is sometimes overlap between the last items of the previous page
    // and the first items of the next page.
    // We remove these duplicates by using a Map.
    return json.features.reduce((map, feature) => {
        return map.set(BigInt(feature.properties.identificatie), feature)
    }, json.features.length > paginationThreshold ? await fetchBag2dPanden(boundingBox, startIndex + json.features.length) : new Map<BigInt, Bag2DPand>())
}

// definitie van de Nederlandse coördinatenstelsel
// bron: iemand op het internet
const RD = '+proj=sterea +lat_0=52.15616055555555 +lon_0=5.38763888888889 +k=0.9999079 +x_0=155000 +y_0=463000 +ellps=bessel +units=m +towgs84=565.2369,50.0087,465.658,-0.406857330322398,0.350732676542563,-1.8703473836068,4.0812 +no_defs'

// Translate coordinate system into a number useful for PDOK BAG service
export function boundingBoxToBAG(boundingBox: LatLngBounds): string {
    const {x: x1, y: y1} = proj4('EPSG:4326', RD, {
        x: boundingBox.getWest(),
        y: boundingBox.getSouth(),
    })
    const {x: x2, y: y2} = proj4('EPSG:4326', RD, {
        x: boundingBox.getEast(),
        y: boundingBox.getNorth(),
    })

    return [x1, y1, x2, y2].join(',')
}
