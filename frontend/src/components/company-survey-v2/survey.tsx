import {css} from '@emotion/react'
import {Alert} from 'antd'
import {cloneDeep} from 'lodash'
import {FunctionComponent, useEffect, useState} from 'react'
import {useForm, UseFormReturn} from 'react-hook-form'
import {useNavigate} from 'react-router-dom'
import {Address} from './address'
import {BasicData} from './basic-data'
import {defineFlash} from './flash'
import {LabelRow} from './generic/label-row'
import {GridConnection} from './grid-connection'
import {HasMultipleConnections} from './has-multiple-connections'
import {Intro} from './intro'
import {ProjectConfiguration} from './project'
import {SurveyTabs} from './survey-tabs'

export const Survey: FunctionComponent<{project: ProjectConfiguration}> = ({project}) => {
    const [key, setKey] = useState(1)

    return (
        <SurveyWithReset key={key} project={project} remount={() => setKey(key + 1)} />
    )
}

export const emptyGridConnection = {
    supply: {}
}

const SurveyWithReset: FunctionComponent<{ project: ProjectConfiguration, remount: () => void }> = ({
    project,
    remount,
}) => {
    let navigate = useNavigate()

    const emptyFormData = {
        tabs: [
            {
                address: {},
                gridConnection: emptyGridConnection,
            },
        ],
    }

    let defaultValues = emptyFormData

    const localStorageKey = `survey-${project.name}`
    const previousData = localStorage.getItem(localStorageKey)
    if (previousData) {
        const previous = JSON.parse(previousData)

        for (const tab of previous.tabs) {
            // these fields were renamed and are now unknown in the back-end
            delete tab.gridConnection.transport.numDailyCarCommuters
            delete tab.gridConnection.transport.numCommuterChargePoints
        }

        defaultValues = previous
    }

    // @ts-ignore
    const form: UseFormReturn = useForm({
        shouldUseNativeValidation: true,
        defaultValues
    })
    const {
        handleSubmit,
        formState: { errors },
        watch
    } = form

    const clear = () => {
        if(confirm("Formulier wissen?")) {
            localStorage.removeItem(localStorageKey)
            remount()
        }
    }

    useEffect(() => {
        const subscription = watch((value, { name, type }) =>
            localStorage.setItem(localStorageKey, JSON.stringify(value))
        )
        return () => subscription.unsubscribe()
    }, [watch])

    const [hasMultipleConnections, setMultipleConnections] = useState(previousData ? defaultValues.tabs.length > 1 : null)
    const [isSuccess, setSuccess] = useState(false)
    const [submissionError, setSubmissionError] = useState("")

    let errorMessage = submissionError
    if (Object.keys(errors).length > 0) {
        errorMessage = "Het formulier bevat fouten"
    }

    const onSubmit = async (surveyData: any) => {
        console.log('submit', surveyData)
        surveyData = cloneDeep(surveyData)
        setSubmissionError("")

        surveyData.zenmoProject = project.name
        surveyData.addresses = []

        for (const tab of surveyData.tabs) {
            if (tab.address.isSameAddress) {
                surveyData.addresses[surveyData.address.length - 1].gridConnections.push(tab.gridConnection)
            } else {
                surveyData.addresses.push({
                    ...tab.address,
                    gridConnections: [tab.gridConnection]
                })
            }
        }

        delete surveyData.tabs

        const url = process.env.ZTOR_URL + '/company-survey'
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                body: JSON.stringify(surveyData)
            })

            if (response.status !== 201) {
                let message = "Er is iets misgegaan."
                setSubmissionError(message)
                const body = await response.json()
                if (body?.error?.message) {
                    message += " Details: " + body.error.message
                    setSubmissionError(message)
                }
                return
            }

            navigate('/bedankt')
        } catch (e) {
            let message = "Er is iets misgegaan."
            // @ts-ignore
            if ('message' in e) {
                message += " Details: " + e.message
            }
            setSubmissionError(message)
            return
        }
    }

    return (
        <div css={[{
            width: '100%',
            minHeight: '100vh',
            backgroundColor: 'grey',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'flex-start',
        }, defineFlash]}>
            <form onSubmit={handleSubmit(onSubmit)} css={{
                maxWidth: '50rem',
                backgroundColor: 'white',
                padding: '2rem',
                marginTop: '2rem',
                boxShadow: '0 0 .15rem .15em white',
                '& input:invalid': {
                    backgroundColor: '#fcc',
                    borderColor: 'red',
                },
                '& h1, & h2, & h3': {
                    padding: '.2rem 1rem',
                    borderBottom: '1px solid #ccc',
                },
                'input[type="text"], input[type="email"], input[type="number"], textarea': {
                    padding: '.3rem',
                }
            }}>
                <Intro project={project}/>
                {errorMessage && <Alert
                    message={errorMessage}
                    type="error"
                    showIcon
                />}
                {isSuccess && <Alert
                    message="Antwoorden opgeslagen. Bedankt voor het invullen."
                    type="success"
                    showIcon
                />}
                <BasicData form={form} />
                <br />
                <HasMultipleConnections
                    hasMultipleConnections={hasMultipleConnections}
                    setMultipleConnections={setMultipleConnections}
                    businessParkname={project.name} />
                <br />
                {hasMultipleConnections === false && (
                    <>
                        <Address form={form} prefix="tabs.0.address" />
                        <GridConnection form={form} prefix="tabs.0.gridConnection" project={project} />
                    </>
                )}
                {hasMultipleConnections === true && (
                    <>
                        <SurveyTabs form={form} project={project} />
                    </>
                )}


                <LabelRow
                    label="Mijn ingevulde gegevens mogen openlijk toegangelijk zijn t.b.v. de energietransitie en voor gesprekken met gemeenten, provincies en andere bedrijventerreinen">
                    <input
                        css={{alignSelf: 'flex-start'}}
                        type="checkbox"
                        {...form.register('dataSharingAgreed')} />
                </LabelRow>

                <div css={css`
                    padding: 1rem 0;
                    display: flex;
                    justify-content: flex-end;

                    & > * {
                        margin-left: 1rem;
                        font-size: 1rem;
                    }
                `}>
                    <button type="button" onClick={clear}>Leegmaken</button>
                    <button type="submit">Verstuur</button>
                </div>
            </form>
        </div>
    )
}
