import {PostcodeKleinverbruik} from './types'

const kleinverbruikUrl = import.meta.env.VITE_KLEINVERBRUIK_URL || 'https://kleinverbruik.thankfulbay-feb62349.westeurope.azurecontainerapps.io'

export const fetchLianderAndStedinKleinverbruik = async (postalCodes: string[]): Promise<PostcodeKleinverbruik[]> => {
    if (postalCodes.length === 0) {
        return []
    }

    const params = new URLSearchParams({
        // the server validates the format of the postal codes
        postalcodes: postalCodes.join(','),
    })

    const url = `${kleinverbruikUrl}?${params.toString()}`
    const response = await fetch(url)
    if (response.status != 200) {
        throw Error('Failure getting Liander and Stedin kleinverbruik data')
    }

    return await response.json()
}
