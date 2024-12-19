import {css} from '@emotion/react'
import {FunctionComponent, PropsWithChildren} from "react"
import {flash} from '../flash'

export const LabelRow: FunctionComponent<PropsWithChildren<{ label: any }>> = ({label, children}) => (
    <label css={[css`
        display: flex;
    `, flash]}>
        <div css={css`
            text-align: right;
            width: 50%;
            padding: 0.3rem;

            display: flex;
            flex-direction: column;
            justify-content: center;
        `}>
            {label}
        </div>
        <div css={css`
            width: 50%;
            padding: 0.3rem;

            display: flex;
            flex-direction: column;
            justify-content: center;
        `}>
            {children}
        </div>
    </label>
)
