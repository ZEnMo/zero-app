import {useState} from "react";
import {useOnce} from "../hooks/use-once";
import {Survey, surveysFromJson} from "zero-zummon"

type UseSurveyReturn = {
    loading: boolean,
    surveys: Survey[],
    // for syncing the state
    changeSurvey: (newSurvey: Survey) => void,
    removeSurvey: (surveyId: string) => void,
}

export const useSurveys = (): UseSurveyReturn => {
    const [loading, setLoading] = useState(true)
    const [surveys, setSurveys] = useState<Survey[]>([])

    const changeSurvey = (newSurvey: Survey) => {
        setSurveys(surveys.map(survey => survey.id.toString() === newSurvey.id.toString() ? newSurvey : survey))
    }

    useOnce(async () => {
        try {
            const response = await fetch(import.meta.env.VITE_ZTOR_URL + '/company-surveys', {
                credentials: 'include',
            })
            if (response.status === 401) {
                redirectToLogin()
                return
            }

            setSurveys(surveysFromJson(await response.text()))
        } catch (error) {
            alert((error as Error).message)
        } finally {
            setLoading(false)
        }
    })

    const removeSurvey = (surveyId: any) => {
        setSurveys(surveys.filter(survey => survey.id.toString() !== surveyId.toString()))
    }

    return {
        loading,
        surveys,
        changeSurvey,
        removeSurvey,
    }
}

export const redirectToLogin = () => {
    window.location.href = import.meta.env.VITE_ZTOR_URL + '/login?redirectUrl=' + encodeURIComponent(window.location.href)
}
