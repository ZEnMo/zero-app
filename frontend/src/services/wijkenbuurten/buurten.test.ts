import {fetchBuurtenByCodes} from "./buurten"

const hoekscheWaardBuurtcodes = [
    "BU19639997",
    "BU19630950",
    "BU19630699",
    "BU19630400",
    "BU19630005",
    "BU19630000",
    "BU19630099",
    "BU19630300",
    "BU19630800",
    "BU19630700",
    "BU19630050",
    "BU19631200",
    "BU19631300",
    "BU19630200",
    "BU19630999",
    "BU19631299",
    "BU19630500",
    "BU19630001",
    "BU19631199",
    "BU19630007",
    "BU19630002",
    "BU19630900",
    "BU19630199",
    "BU19630006",
    "BU19631099",
    "BU19630150",
    "BU19630399",
    "BU19631399",
    "BU19630499",
    "BU19630600",
    "BU19630299",
    "BU19630750",
    "BU19631000",
    "BU19630799",
    "BU19630599",
    "BU19630051",
    "BU19630650",
    "BU19630003",
    "BU19630100",
    "BU19630004",
    "BU19631100",
    "BU19630899",
]

describe(fetchBuurtenByCodes.name, () => {
    test("Many buurtcodes which split request", async () => {
        const buurten = await fetchBuurtenByCodes(hoekscheWaardBuurtcodes)
        expect(buurten.features.length).toBe(hoekscheWaardBuurtcodes.length)
    })
})
