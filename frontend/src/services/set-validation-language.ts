
import {Language, setLanguage} from "zero-zummon"

export function setValidationLanguage(): void {
    for (const language of navigator.languages) {
        if (language.startsWith(Language.en.name)) {
            setLanguage(Language.en)
            return
        }

        if (language.startsWith(Language.nl.name)) {
            setLanguage(Language.nl)
            return
        }
    }

    // No matching language.
    // A default fallback is already present.
}
