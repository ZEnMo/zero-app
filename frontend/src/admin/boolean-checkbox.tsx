import {FunctionComponent} from "react"
import {ClassNames} from "@emotion/react"
import {MultiStateCheckbox} from "primereact/multistatecheckbox"

/**
 * Generic checkbox which behaves like a toggle
 */
export const BooleanCheckbox: FunctionComponent<{
    value: boolean,
    disabled?: boolean,
    onChange: (newValue: boolean) => void
}> = ({value, disabled = false, onChange})  => {
    const options = [
        {
            value: false,
            icon: "pi pi-times",
        },
        {
            value: true,
            icon: "pi pi-check",
        },
    ];

    const color = value ? "lightgreen" : "red"

    return (
        <ClassNames>
            {({ css, cx }) => (
                <MultiStateCheckbox
                    value={value}
                    options={options}
                    optionValue="value"
                    empty={false}
                    onChange={(e) => { onChange(e.value) }}
                    disabled={disabled}
                    pt={{
                        root: {
                            className: css`
                                & .p-checkbox-box {
                                    background-color: ${color};
                                    border-color: ${color};
                                }
                            `
                        },
                    }}
                />
            )}
        </ClassNames>
    )
}
