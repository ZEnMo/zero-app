import {css} from '@emotion/react'
import {Button, Input, InputNumber, Upload, Form, Card, Alert} from 'antd'
import { UploadOutlined } from '@ant-design/icons';
import {useForm} from 'antd/es/form/Form'
import {FunctionComponent, useRef, useState} from 'react'

const {TextArea} = Input

export const BedrijvenFormV1: FunctionComponent = () => {
    const [form] = useForm()
    const [error, setError] = useState("")
    const [success, setSuccess] = useState("")

    const onFinish = async (values: any) => {
        setSuccess("")
        setError("")
        values = {...values}
        values.electricityConnections = values.electricityConnections.map(
            (e: any) => {
                e.quarterlyValuesFile = e.quarterlyValuesFiles[0] ?? ""
                delete e.quarterlyValuesFiles
                return e
            }
        )

        delete values.numberOfElectricityConnections

        const url = import.meta.env.VITE_ZTOR_URL + '/company-surveys'
        try {
            const response = await fetch(url, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                },
                body: JSON.stringify(values),
            })

            if (response.status !== 201) {
                let message = "Er is iets misgegaan."
                setError(message)
                const body = await response.json()
                if (body?.error?.message) {
                    message += " Details: " + body.error.message
                    setError(message)
                }
            }
        } catch (e) {
            let message = "Er is iets misgegaan."
            // @ts-ignore
            if ('message' in e) {
                message += " Details: " + e.message
            }
            setError(message)
            return
        }

        setSuccess("Antwoorden opgeslagen. Bedankt voor het invullen.")
    };

    const onFinishFailed = (errorInfo: any) => {
    };

    if (success) {
        return <Alert
            message={success}
            type="success"
            showIcon
        />
    }

    return (
        <Form
            style={{
                maxWidth: '60rem',
                padding: '1rem',
            }}
            colon={false}
            labelWrap={true}
            labelCol={{ span: 8 }}
            wrapperCol={{ span: 16 }}
            form={form}
            onFieldsChange={() => setSuccess("")}
            onFinish={onFinish}
            onFinishFailed={onFinishFailed}
        >
            <h1>Opgeven energieprofiel bedrijf</h1>
            <p>
                Dit formulier is bedoeld voor een bedrijf om zijn situatie te omschrijven,
                zodat dit verwerkt kan worden in de simulatie van een bedrijventerrein.
            </p>
            <p>
                Het aantal vrije invulvelden is beperkt zodat de gegevens direct in de simulatie gebruikt kunnen worden.
            </p>
            <p>
                Dit valt onder het project "Digital twins energiesysteem Drechtsteden".
                Dit project heeft tot doel om het energiesysteem te verduurzamen en oplossingen te zoeken voor netcongestie.
                De opdrachtgever is XXXXXXX en de uitvoerder is Zenmo Simulations. Zenmo staat voor Zero emission Energy and Mobility.
            </p>
            {error && <Alert
                message={error}
                type="error"
                showIcon
            />}
            <br />
            <Form.Item
                label="Naam bedrijf"
                name="companyName"
                required
                rules={[{required: true}]}
            >
                <Input />
            </Form.Item>
            <Form.Item
                label="Naam contactpersoon"
                name="personName"
                required
                rules={[{required: true}]}
            >
                <Input />
            </Form.Item>
            <Form.Item label="E-mailadres" name="email" initialValue="">
                <Input type="email" />
            </Form.Item>
            <Form.Item label="Welke assets heb je die energie verbruiken?" name="usageAssets" initialValue="">
                <TextArea autoSize={{minRows: 4}}/>
            </Form.Item>
            <Form.Item label="Welke assets heb je voor opwek of opslag van energie?" name="generationAssets" initialValue="">
                <TextArea autoSize={{minRows: 4}} />
            </Form.Item>
            <Form.Item
                name="usagePattern"
                css={css`
                    label {
                      height: auto !important;
                      flex-direction: column;
                    }
                `}
                label={
                    <>
                        <div>Beschrijf het patroon van energieverbruik</div>
                        <ul style={{textAlign: 'left'}}>
                            <li>Type dragers (elektriciteit, gas, diesel, waterstof, ...)</li>
                            <li>Hoeveelheden</li>
                            <li>Seizoenverschillen</li>
                            <li>Max ~10 regels</li>
                        </ul>
                    </>
                }
                initialValue="">
                <TextArea autoSize={{minRows: 4}} />
            </Form.Item>
            <Netaansluitingen />
            <div>
                <Button type="primary" htmlType="submit">
                    Verstuur
                </Button>
            </div>
        </Form>
    )
}

const Netaansluitingen: FunctionComponent = () => {
    const [form] = useForm()

    const listRef = useRef({
        length: 0,
        add: () => {},
        remove: (i: number) => {},
    })

    return (
        <>
            <h1>Netaansluiting</h1>
            <p>
                Als u geen volledige informatie heeft, vul dan in wat u wel weet.
            </p>
            <Form.Item
                label="Hoeveel aansluitingen heeft u op het elektriciteitsnet"
                name="numberOfElectricityConnections"
                required
                rules={[{required: true}]}>
                <InputNumber min="1" onChange={
                    (value) => {
                        // @ts-ignore
                        const int = parseInt(value)
                        if (Number.isNaN(int)) {
                            return
                        }

                        for (let i = listRef.current.length; i < int; i++) {
                            listRef.current.add()
                        }
                        for (let i = listRef.current.length; i > int; i--) {
                            listRef.current.remove(i - 1)
                        }
                    }
                }/>
            </Form.Item>
            <Form.List name="electricityConnections">
                {(fields, {add, remove}) => {
                    listRef.current = {length: fields.length, add, remove}

                    return fields.map((field, index) => (
                        <Netaansluiting key={index} index={index} fieldName={field.name}/>
                    ))
                }}
            </Form.List>
        </>
    )
}

const Netaansluiting: FunctionComponent<{ index: number, fieldName: any }> = ({index, fieldName}) => {
    return (
        <Card title={`${index + 1}e netaansluiting`}>
            <Form.Item name={[fieldName, 'street']} label="Straat" required initialValue="" rules={[{required: true}]}>
                <Input/>
            </Form.Item>
            <Form.Item name={[fieldName, 'houseNumber']} label="Huisnummer" required rules={[{required: true}]}>
                {/*@ts-ignore*/}
                <InputNumber style={{width: '5rem'}}/>
            </Form.Item>
            <Form.Item name={[fieldName, 'houseLetter']} label="Huisletter" initialValue="">
                <Input pattern="^[a-zA-Z]$" style={{width: '5rem'}}/>
            </Form.Item>
            <Form.Item name={[fieldName, 'houseNumberSuffix']} label="Toevoeging" initialValue="">
                <Input style={{width: '5rem'}}/>
            </Form.Item>
            <Form.Item name={[fieldName, 'annualUsageKWh']} label="Jaarverbruik" initialValue={null}>
                <InputNumber min="0" style={{width: '10rem'}} addonAfter="kWh" />
            </Form.Item>
            <Form.Item
                name={[fieldName, 'quarterlyValuesFiles']}
                label="Kwartierwaarden van het afgelopen jaar of meerdere jaren. Deze kunt u opvragen bij uw meetbedrijf of energieleverancier."
                getValueFromEvent={(e) => {console.log(e); return ['asdf']}}
                valuePropName="fileList"
                initialValue={[]}
            >
                <Upload>
                    <Button icon={<UploadOutlined />}>Uploaden</Button>
                </Upload>
            </Form.Item>
            <Form.Item name={[fieldName, 'ean']} label="EAN-code" initialValue="">
                <Input />
            </Form.Item>
            <Form.Item name={[fieldName, 'description']} label="toelichting" initialValue="">
                <TextArea autoSize={{minRows: 2}} />
            </Form.Item>
        </Card>
    )
}
