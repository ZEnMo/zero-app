import {UploadOutlined} from '@ant-design/icons'
import {Button, Upload as AntdUpload, UploadProps} from 'antd'
import {RcFile} from 'antd/es/upload'
import {FunctionComponent} from 'react'
import {ProjectName} from '../project'

type UploadAuthorization = {
    uploadUrl: string
    blobName: string
    originalName: string
    /**
     * Shared Access Signature (SAS) token
     */
    sas: string
}

type UploadedFile = {
    blobName: string
    originalName: string
    contentType: string | undefined
    size: number | undefined
}

export enum Purpose {
    NATURAL_GAS_VALUES = 'NATURAL_GAS_VALUES',
    ELECTRICITY_VALUES = 'ELECTRICITY_VALUES',
    ELECTRICITY_AUTHORIZATION = 'ELECTRICITY_AUTHORIZATION',
}

export const Upload: FunctionComponent<{
    multiple: true
    setFormValue: (files: UploadedFile[]) => void
    company: string
    project: ProjectName
    purpose: Purpose
} | {
    multiple: false
    setFormValue: (file: UploadedFile) => void
    company: string
    project: ProjectName
    purpose: Purpose
}> = ({multiple, setFormValue, project, company, purpose }) => {
    const getUploadUrl = async (file: RcFile) => {
        const queryParams = new URLSearchParams({
            fileName: file.name,
            project: project,
            company: company,
            purpose: purpose,
        })
        const response = await fetch(import.meta.env.VITE_ZTOR_URL + '/upload-url?' + queryParams.toString())
        const uploadAuthorization: UploadAuthorization = await response.json()

        // @ts-ignore
        file.blobName = uploadAuthorization.blobName
        return uploadAuthorization.uploadUrl
    }

    const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
        try {
            const uploadUrl = await getUploadUrl(file)
            const response = await fetch(uploadUrl, {
                method: 'PUT',
                headers: {
                    'Content-Type': file.type,
                    'x-ms-blob-type': 'BlockBlob',
                    // Unfortunately blob storage rejects the upload in case of special characters
                    // 'x-ms-meta-originalName': file.name,
                    // 'x-ms-meta-company': company,
                    'x-ms-meta-project': project,
                },
                body: file,
            })
            if (response.status !== 201) {
                alert('Uploaden mislukt')
                // it will fail later and cause a red color
                return true
            }
        } catch (e) {
            alert('Uploaden mislukt')
            // it will fail later and cause a red color
            return true
        }

        return false
    }

    const onChange: UploadProps['onChange'] = ({fileList}) => {
        const formValue = fileList.map(file => ({
            originalName: file.name,
            // @ts-ignore
            blobName: file.blobName,
            size: file.size,
            contentType: file.type,
        }))
        if (multiple) {
            setFormValue(formValue)
        } else {
            setFormValue(formValue[0])
        }
    }

    return (
        <AntdUpload action="https://nonexistent.asdf" multiple={multiple} beforeUpload={beforeUpload} onChange={onChange}>
            <Button icon={<UploadOutlined />}>Uploaden</Button>
        </AntdUpload>
    )
}